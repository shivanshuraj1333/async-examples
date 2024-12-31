package app

import (
	"context"
	"math/rand"
	"sync/atomic"
	"time"

	"github.com/shivanshuraj13333/async-examples/async-go/internal/config"
	"github.com/shivanshuraj13333/async-examples/async-go/internal/metrics"
	"github.com/shivanshuraj13333/async-examples/async-go/internal/types"
	"github.com/shivanshuraj13333/async-examples/async-go/internal/worker"
	"go.uber.org/zap"
	"golang.org/x/sync/errgroup"
)

type App struct {
	config  config.Config
	logger  *zap.Logger
	metrics *metrics.Metrics
}

func New(cfg config.Config, logger *zap.Logger) *App {
	return &App{
		config:  cfg,
		logger:  logger,
		metrics: metrics.New(),
	}
}

func (a *App) Run(ctx context.Context) error {
	channel := make(chan types.Message, a.config.ChannelBuffer)
	g, ctx := errgroup.WithContext(ctx)

	// Track number of active producers
	var activeProducers atomic.Int32

	// Start producers
	for p := 1; p <= a.config.NumProducers; p++ {
		activeProducers.Add(1)
		producer := worker.NewProducer(p, worker.ProducerConfig{
			MessagesCount: a.config.NumMessagesPerProducer,
			MinDelay:      100 * time.Millisecond,
			MaxDelay:      500 * time.Millisecond,
		}, a.metrics, a.logger)

		g.Go(func() error {
			defer activeProducers.Add(-1)
			return producer.Run(ctx, channel)
		})
	}

	// Start consumers
	for c := 1; c <= a.config.NumConsumers; c++ {
		consumer := worker.NewConsumer(c, worker.ConsumerConfig{
			MinDelay: 200 * time.Millisecond,
			MaxDelay: 600 * time.Millisecond,
		}, a.metrics, a.logger)

		g.Go(func() error {
			for {
				select {
				case <-ctx.Done():
					return ctx.Err()
				case msg, ok := <-channel:
					if !ok {
						return nil
					}
					if err := consumer.ProcessMessage(ctx, msg, rand.New(rand.NewSource(time.Now().UnixNano()))); err != nil {
						return err
					}
				default:
					if activeProducers.Load() == 0 && len(channel) == 0 {
						return nil
					}
				}
			}
		})
	}

	// Monitor metrics
	g.Go(func() error {
		ticker := time.NewTicker(5 * time.Second)
		defer ticker.Stop()

		for {
			select {
			case <-ctx.Done():
				return ctx.Err()
			case <-ticker.C:
				if activeProducers.Load() == 0 && len(channel) == 0 {
					// Print final metrics and message timings
					a.metrics.PrintStats(a.logger)
					a.logger.Info("Final message processing times:")
					a.metrics.PrintMessageTimings(a.logger)
					return nil
				}
				a.metrics.PrintStats(a.logger)
			}
		}
	})

	// Wait for all goroutines to complete
	err := g.Wait()

	// Print final timings even if there was an error
	if err != nil {
		a.logger.Error("Application error", zap.Error(err))
	}

	a.logger.Info("Final message processing times:")
	a.metrics.PrintMessageTimings(a.logger)

	return err
}
