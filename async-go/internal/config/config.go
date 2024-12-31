package config

import (
	"github.com/spf13/cobra"
)

type Config struct {
	NumProducers           int
	NumConsumers           int
	NumMessagesPerProducer int
	ChannelBuffer          int
}

func Load() Config {
	config := Config{
		ChannelBuffer: 100,
	}

	rootCmd := &cobra.Command{
		Use:   "producer-consumer",
		Short: "A producer-consumer application",
	}

	rootCmd.Flags().IntVarP(&config.NumProducers, "producers", "p", 10, "Number of producers")
	rootCmd.Flags().IntVarP(&config.NumConsumers, "consumers", "c", 5, "Number of consumers")
	rootCmd.Flags().IntVarP(&config.NumMessagesPerProducer, "messages", "m", 5, "Number of messages per producer")

	if err := rootCmd.Execute(); err != nil {
		panic(err)
	}

	return config
}
