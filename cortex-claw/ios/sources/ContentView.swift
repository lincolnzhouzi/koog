import SwiftUI

public struct ContentView: View {
    @StateObject private var viewModel: MainViewModel
    @State private var inputText: String = ""
    
    public init(client: CortexClawClient) {
        _viewModel = StateObject(wrappedValue: MainViewModel(client: client))
    }
    
    public var body: some View {
        NavigationView {
            VStack {
                messageListView
                inputView
            }
            .navigationTitle("Cortex Claw")
            .alert("Error", isPresented: .init(
                get: { viewModel.error != nil },
                set: { if !$0 { viewModel.clearError() } }
            )) {
                Button("OK") {
                    viewModel.clearError()
                }
            } message: {
                Text(viewModel.error ?? "")
            }
        }
    }
    
    private var messageListView: some View {
        ScrollViewReader { proxy in
            List {
                ForEach(viewModel.messages) { message in
                    MessageView(message: message)
                        .id(message.id)
                }
            }
            .onChange(of: viewModel.messages.count) { _ in
                if let lastMessage = viewModel.messages.last {
                    withAnimation {
                        proxy.scrollTo(lastMessage.id, anchor: .bottom)
                    }
                }
            }
        }
    }
    
    private var inputView: some View {
        HStack {
            TextField("Type a message...", text: $inputText)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .disabled(viewModel.isLoading)
            
            Button(action: sendMessage) {
                if viewModel.isLoading {
                    ProgressView()
                } else {
                    Image(systemName: "paperplane.fill")
                }
            }
            .disabled(inputText.isEmpty || viewModel.isLoading)
        }
        .padding()
    }
    
    private func sendMessage() {
        let text = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        
        inputText = ""
        
        Task {
            await viewModel.sendMessage(text)
        }
    }
}

public struct MessageView: View {
    public let message: ChatMessage
    
    public var body: some View {
        HStack {
            if message.isUser {
                Spacer()
                Text(message.content)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(16)
            } else {
                Text(message.content)
                    .padding()
                    .background(Color.gray.opacity(0.2))
                    .cornerRadius(16)
                Spacer()
            }
        }
        .listRowSeparator(.hidden)
    }
}
