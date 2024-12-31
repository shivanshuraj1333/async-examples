package worker

import (
	"context"
	"math/rand"
	"time"

	"github.com/google/uuid"
	"github.com/shivanshuraj13333/async-examples/async-go/internal/types"
	"go.uber.org/zap"
)

type MetricsRecorder interface {
	RecordStart(msg types.Message)
	RecordEnd(msg types.Message)
	IncrementActiveProducers()
	DecrementActiveProducers()
	IncrementActiveConsumers()
	DecrementActiveConsumers()
	IncrementTotalMessages()
	IncrementErrors()
}

type Producer struct {
	id      int
	config  ProducerConfig
	metrics MetricsRecorder
	logger  *zap.Logger
}

type ProducerConfig struct {
	MessagesCount int
	MinDelay      time.Duration
	MaxDelay      time.Duration
}

func NewProducer(id int, config ProducerConfig, metrics MetricsRecorder, logger *zap.Logger) *Producer {
	return &Producer{
		id:      id,
		config:  config,
		metrics: metrics,
		logger:  logger,
	}
}

func (p *Producer) Run(ctx context.Context, sender chan<- types.Message) error {
	p.metrics.IncrementActiveProducers()
	defer p.metrics.DecrementActiveProducers()

	rng := rand.New(rand.NewSource(time.Now().UnixNano()))

	for i := 0; i < p.config.MessagesCount; i++ {
		select {
		case <-ctx.Done():
			return ctx.Err()
		default:
			msg := types.Message{
				ID:        uuid.New().String(),
				Timestamp: time.Now().UnixNano(),
			}

			delay := time.Duration(rng.Int63n(int64(p.config.MaxDelay-p.config.MinDelay))) + p.config.MinDelay
			time.Sleep(delay)

			if err := p.sendMessage(ctx, sender, msg, i+1); err != nil {
				p.metrics.IncrementErrors()
				return err
			}
		}
	}

	p.logger.Info("Producer completed", zap.Int("producer_id", p.id))
	return nil
}

func (p *Producer) sendMessage(ctx context.Context, sender chan<- types.Message, msg types.Message, msgNum int) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	case sender <- msg:
		p.metrics.RecordStart(msg)
		p.metrics.IncrementTotalMessages()
		p.logger.Info("Message sent",
			zap.Int("producer_id", p.id),
			zap.String("message_id", msg.ID),
			zap.Int("message_num", msgNum),
		)
		return nil
	}
}
