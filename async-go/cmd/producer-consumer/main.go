package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"strings"
	"syscall"

	"github.com/shivanshuraj13333/async-examples/async-go/internal/app"
	"github.com/shivanshuraj13333/async-examples/async-go/internal/config"
	"github.com/shivanshuraj13333/async-examples/async-go/pkg/logger"
	"go.uber.org/zap"
)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	log := logger.NewLogger()

	// Handle shutdown signals
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)

	go func() {
		<-sigChan
		cancel()
	}()

	cfg := config.Load()
	application := app.New(cfg, log)

	err := application.Run(ctx)

	// Handle application error
	if err != nil {
		log.Error("Application error", zap.Error(err))
	}

	// Sync logger and handle any sync errors
	if syncErr := log.Sync(); syncErr != nil {
		// Only print to stderr if it's not a "bad file descriptor" error
		if !os.IsNotExist(syncErr) && !strings.Contains(syncErr.Error(), "bad file descriptor") {
			fmt.Fprintf(os.Stderr, "Failed to sync logger: %v\n", syncErr)
		}
	}

	// Exit with appropriate code
	if err != nil {
		os.Exit(1)
	}
}
