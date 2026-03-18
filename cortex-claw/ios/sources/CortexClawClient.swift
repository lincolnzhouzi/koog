import Foundation
import Combine

@objc public class CortexClawClient: NSObject, ObservableObject {
    @Published public private(set) var isInitialized: Bool = false
    @Published public private(set) var initializationError: String?
    
    private var agent: CortexClawAgent?
    private var deviceManager: DeviceManager?
    private var profileManager: UserProfileManager?
    
    public override init() {
        super.init()
        initializeComponents()
    }
    
    private func initializeComponents() {
        Task {
            do {
                self.deviceManager = DeviceManager.getInstance()
                try await self.deviceManager?.initialize()
                
                self.profileManager = UserProfileManager()
                try await self.profileManager?.initialize(userId: "default-user")
                
                let promptExecutor = MNNPromptExecutor()
                
                self.agent = CortexClawAgent(
                    promptExecutor: promptExecutor,
                    config: AgentConfig(),
                    deviceManager: self.deviceManager!,
                    profileManager: self.profileManager
                )
                
                try await self.agent?.initialize()
                
                await MainActor.run {
                    self.isInitialized = true
                }
            } catch {
                await MainActor.run {
                    self.initializationError = error.localizedDescription
                }
            }
        }
    }
    
    public func processInput(_ input: String) async throws -> String {
        guard let agent = agent else {
            throw CortexClawError.agentNotInitialized
        }
        
        var result = ""
        for try await response in try await agent.processInput(input) {
            switch response {
            case let .text(content):
                result += content
            case let .streaming(chunk):
                result += chunk
            case let .error(message):
                throw CortexClawError.processingError(message)
            default:
                break
            }
        }
        return result
    }
    
    public func shutdown() async {
        await agent?.shutdown()
        await deviceManager?.shutdown()
        await MainActor.run {
            self.isInitialized = false
        }
    }
}

public enum CortexClawError: Error, LocalizedError {
    case agentNotInitialized
    case processingError(String)
    case deviceNotFound(String)
    case modelLoadError(String)
    
    public var errorDescription: String? {
        switch self {
        case .agentNotInitialized:
            return "Agent is not initialized"
        case .processingError(let message):
            return "Processing error: \(message)"
        case .deviceNotFound(let id):
            return "Device not found: \(id)"
        case .modelLoadError(let message):
            return "Model load error: \(message)"
        }
    }
}
