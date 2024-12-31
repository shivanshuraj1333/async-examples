use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::time::{Duration, Instant};
use tokio::sync::broadcast;
use tokio::task;
use clap::{App, Arg};
use log::{info, error};

type Message = String;

struct MessageDuration {
    start: Instant,
    end: Option<Instant>,
}

#[tokio::main]
async fn main() {
    env_logger::init();

    let matches = App::new("Producer-Consumer")
        .about("A producer-consumer application")
        .arg(Arg::with_name("producers")
            .short('p')
            .long("producers")
            .value_name("NUM_PRODUCERS")
            .default_value("10")
            .help("Number of producers"))
        .arg(Arg::with_name("consumers")
            .short('c')
            .long("consumers")
            .value_name("NUM_CONSUMERS")
            .default_value("5")
            .help("Number of consumers"))
        .arg(Arg::with_name("messages")
            .short('m')
            .long("messages")
            .value_name("NUM_MESSAGES_PER_PRODUCER")
            .default_value("5")
            .help("Number of messages per producer"))
        .get_matches();

    let num_producers = matches.value_of("producers").unwrap().parse().unwrap();
    let num_consumers = matches.value_of("consumers").unwrap().parse().unwrap();
    let num_messages_per_producer = matches.value_of("messages").unwrap().parse().unwrap();

    info!("Starting with {} producers and {} consumers", num_producers, num_consumers);

    setup(num_producers, num_consumers, num_messages_per_producer).await;
}

async fn setup(num_producers: usize, num_consumers: usize, num_messages_per_producer: usize) {
    let record = Arc::new(Mutex::new(HashMap::new()));
    run(num_producers, num_consumers, num_messages_per_producer, record.clone()).await;
    info!("################ printing produce and consume delays ################");
    tokio::time::sleep(Duration::from_secs(2)).await;
    print_delay(record);
    info!("################ printing done ################");
}

async fn run(num_producers: usize, num_consumers: usize, num_messages_per_producer: usize, record: Arc<Mutex<HashMap<Message, MessageDuration>>>) {
    let (tx, _rx) = broadcast::channel::<Message>(100);

    let mut producer_handles = Vec::new();
    let mut consumer_handles = Vec::new();

    // Spawn producers
    for p in 1..=num_producers {
        let tx_clone = tx.clone();
        let record_clone = record.clone();
        let handle = task::spawn(async move {
            producer(p, tx_clone, num_messages_per_producer, record_clone).await;
        });
        producer_handles.push(handle);
    }

    // Spawn consumers
    let consumed = Arc::new(Mutex::new(Vec::new()));
    for c in 1..=num_consumers {
        let rx = tx.subscribe();
        let consumed_clone = consumed.clone();
        let record_clone = record.clone();
        let handle = task::spawn(async move {
            consumer(c, rx, consumed_clone, record_clone).await;
        });
        consumer_handles.push(handle);
    }

    // Wait for all producers to complete
    for handle in producer_handles {
        handle.await.unwrap();
    }

    // Drop the sender to signal no more messages will be sent
    drop(tx);

    // Wait for all consumers to complete
    for handle in consumer_handles {
        handle.await.unwrap();
    }

    info!("All producers and consumers have completed.");
    let consumed_messages = consumed.lock().unwrap();
    info!("Number of messages consumed: {}", consumed_messages.len());
}

async fn producer(id: usize, sender: broadcast::Sender<Message>, num_messages: usize, record: Arc<Mutex<HashMap<Message, MessageDuration>>>) {
    for _ in 0..num_messages {
        let msg = uuid::Uuid::new_v4().to_string();
        let delay = rand::random::<u64>() % 400 + 100;
        tokio::time::sleep(Duration::from_millis(delay)).await;
        if let Err(e) = sender.send(msg.clone()) {
            error!("Producer {} failed to send message: {}", id, e);
            return;
        }
        record.lock().unwrap().insert(msg.clone(), MessageDuration { start: Instant::now(), end: None });
        info!("Producer {} sent {}", id, msg);
    }
    info!("Producer {} completed", id);
}

async fn consumer(
    id: usize,
    mut receiver: broadcast::Receiver<Message>,
    consumed: Arc<Mutex<Vec<Message>>>,
    record: Arc<Mutex<HashMap<Message, MessageDuration>>>,
) {
    while let Ok(msg) = receiver.recv().await {
        // Skip if we've already processed this message (broadcast channels send to all consumers)
        {
            let mut consumed_msgs = consumed.lock().unwrap();
            if consumed_msgs.contains(&msg) {
                continue;
            }
            // Process the message only if we haven't seen it before
            info!("Consumer {} received {}", id, msg);
            consumed_msgs.push(msg.clone());
            if let Some(duration) = record.lock().unwrap().get_mut(&msg) {
                duration.end = Some(Instant::now());
            }
        } // Lock is released here

        let delay = rand::random::<u64>() % 400 + 200;
        tokio::time::sleep(Duration::from_millis(delay)).await;
    }
    info!("Consumer {} completed", id);
}

fn print_delay(record: Arc<Mutex<HashMap<Message, MessageDuration>>>) {
    let record = record.lock().unwrap();
    for (msg, duration) in record.iter() {
        if let Some(end) = duration.end {
            let delay = end.duration_since(duration.start).as_millis();
            info!("Message {} took {} ms", msg, delay);
        }
    }
}

