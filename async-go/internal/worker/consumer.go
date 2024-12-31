package worker

import (
	"context"
	"math/rand"
	"time"

	"github.com/shivanshuraj13333/async-examples/async-go/internal/types"
	"go.uber.org/zap"
)

type Consumer struct {
	id      int
	metrics MetricsRecorder
	logger  *zap.Logger
	config  ConsumerConfig
}

type ConsumerConfig struct {
	MinDelay time.Duration
	MaxDelay time.Duration
}

func NewConsumer(id int, config ConsumerConfig, metrics MetricsRecorder, logger *zap.Logger) *Consumer {
	return &Consumer{
		id:      id,
		config:  config,
		metrics: metrics,
		logger:  logger,
	}
}

func (c *Consumer) Run(ctx context.Context, receiver <-chan types.Message) error {
	c.metrics.IncrementActiveConsumers()
	defer c.metrics.DecrementActiveConsumers()

	rng := rand.New(rand.NewSource(time.Now().UnixNano()))

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case msg, ok := <-receiver:
			if !ok {
				c.logger.Info("Consumer completed", zap.Int("consumer_id", c.id))
				return nil
			}

			if err := c.ProcessMessage(ctx, msg, rng); err != nil {
				c.metrics.IncrementErrors()
				return err
			}
		}
	}
}

func (c *Consumer) ProcessMessage(ctx context.Context, msg types.Message, rng *rand.Rand) error {
	delay := time.Duration(rng.Int63n(int64(c.config.MaxDelay-c.config.MinDelay))) + c.config.MinDelay

	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(delay):
		c.metrics.RecordEnd(msg)
		c.logger.Info("Message processed",
			zap.Int("consumer_id", c.id),
			zap.String("message_id", msg.ID),
		)
		return nil
	}
}
