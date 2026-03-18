import SwiftUI

@main
public struct CortexClawApp: App {
    @StateObject private var client = CortexClawClient()
    
    public init() {}
    
    public var body: some Scene {
        WindowGroup {
            if client.isInitialized {
                ContentView(client: client)
            } else if let error = client.initializationError {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    
                    Text("Initialization Failed")
                        .font(.headline)
                    
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding()
                    
                    Button("Retry") {
                        Task {
                            await client.shutdown()
                        }
                    }
                    .buttonStyle(.bordered)
                }
            } else {
                VStack(spacing: 16) {
                    ProgressView()
                        .scaleEffect(1.5)
                    
                    Text("Initializing Cortex Claw...")
                        .font(.headline)
                }
            }
        }
    }
}
