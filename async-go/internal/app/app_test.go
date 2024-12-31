package app

import (
	"context"
	"testing"
	"time"

	"github.com/shivanshuraj13333/async-examples/async-go/internal/config"
	"github.com/shivanshuraj13333/async-examples/async-go/pkg/logger"
)

func TestApp(t *testing.T) {
	tests := []struct {
		name      string
		config    config.Config
		wantMsgs  int
		wantError bool
	}{
		{
			name: "Basic operation",
			config: config.Config{
				NumProducers:           2,
				NumConsumers:           2,
				NumMessagesPerProducer: 5,
				ChannelBuffer:          10,
			},
			wantMsgs:  10,
			wantError: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			log := logger.NewLogger()
			app := New(tt.config, log)

			ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
			defer cancel()

			err := app.Run(ctx)
			if (err != nil) != tt.wantError {
				t.Errorf("Run() error = %v, wantError %v", err, tt.wantError)
			}
		})
	}
}
