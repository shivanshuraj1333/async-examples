package metrics

import (
	"sync"
	"sync/atomic"
	"time"

	"github.com/shivanshuraj13333/async-examples/async-go/internal/types"
	"go.uber.org/zap"
)

type Metrics struct {
	mu     sync.RWMutex
	record map[string]*types.Duration

	totalMessages   atomic.Int64
	activeProducers atomic.Int32
	activeConsumers atomic.Int32
	errors          atomic.Int64
}

func New() *Metrics {
	return &Metrics{
		record: make(map[string]*types.Duration),
	}
}

func (m *Metrics) RecordStart(msg types.Message) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.record[msg.ID] = &types.Duration{Start: time.Now().UnixNano()}
}

func (m *Metrics) RecordEnd(msg types.Message) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if d, exists := m.record[msg.ID]; exists {
		d.End = time.Now().UnixNano()
	}
}

func (m *Metrics) PrintStats(logger *zap.Logger) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var totalLatency int64
	var processedCount int64

	for _, d := range m.record {
		if d.End > 0 {
			totalLatency += (d.End - d.Start)
			processedCount++
		}
	}

	var avgLatency int64
	if processedCount > 0 {
		avgLatency = totalLatency / processedCount / int64(time.Millisecond)
	}

	logger.Info("Application metrics",
		zap.Int64("total_messages", m.totalMessages.Load()),
		zap.Int32("active_producers", m.activeProducers.Load()),
		zap.Int32("active_consumers", m.activeConsumers.Load()),
		zap.Int64("errors", m.errors.Load()),
		zap.Int64("processed_messages", processedCount),
		zap.Int64("avg_latency_ms", avgLatency),
	)
}

func (m *Metrics) ActiveProducers() *atomic.Int32 { return &m.activeProducers }
func (m *Metrics) TotalMessages() *atomic.Int64   { return &m.totalMessages }
func (m *Metrics) Errors() *atomic.Int64          { return &m.errors }

func (m *Metrics) IncrementActiveProducers() { m.activeProducers.Add(1) }
func (m *Metrics) DecrementActiveProducers() { m.activeProducers.Add(-1) }
func (m *Metrics) IncrementActiveConsumers() { m.activeConsumers.Add(1) }
func (m *Metrics) DecrementActiveConsumers() { m.activeConsumers.Add(-1) }
func (m *Metrics) IncrementTotalMessages()   { m.totalMessages.Add(1) }
func (m *Metrics) IncrementErrors()          { m.errors.Add(1) }

func (m *Metrics) PrintMessageTimings(logger *zap.Logger) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	for msgID, d := range m.record {
		if d.End > 0 { // Only print completed messages
			processingTime := (d.End - d.Start) / int64(time.Millisecond)
			logger.Info("Message processing time",
				zap.String("message_id", msgID),
				zap.Int64("duration_ms", processingTime),
			)
		}
	}
}
