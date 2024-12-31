package worker

// Message represents a message in the system
type Message struct {
	ID        string
	Timestamp int64
}

// Duration tracks message timing
type Duration struct {
	Start int64
	End   int64
}

// Result represents processing result
type Result struct {
	Message Message
	Error   error
}
