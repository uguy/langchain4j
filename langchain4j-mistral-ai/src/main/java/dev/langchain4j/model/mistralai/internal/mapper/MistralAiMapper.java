package dev.langchain4j.model.mistralai.internal.mapper;

import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.JsonSchemaElementUtils.toMap;
import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.mistralai.internal.api.MistralAiChatMessageContent.DocumentUrlContent;
import static dev.langchain4j.model.mistralai.internal.api.MistralAiChatMessageContent.ImageUrlContent;
import static dev.langchain4j.model.mistralai.internal.api.MistralAiChatMessageContent.ReferenceContent;
import static dev.langchain4j.model.output.FinishReason.CONTENT_FILTER;
import static dev.langchain4j.model.output.FinishReason.LENGTH;
import static dev.langchain4j.model.output.FinishReason.STOP;
import static dev.langchain4j.model.output.FinishReason.TOOL_EXECUTION;
import static java.util.stream.Collectors.toList;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.mistralai.internal.api.MistralAiChatCompletionResponse;
import dev.langchain4j.model.mistralai.internal.api.MistralAiChatMessage;
import dev.langchain4j.model.mistralai.internal.api.MistralAiChatMessageContent;
import dev.langchain4j.model.mistralai.internal.api.MistralAiFunction;
import dev.langchain4j.model.mistralai.internal.api.MistralAiFunctionCall;
import dev.langchain4j.model.mistralai.internal.api.MistralAiParameters;
import dev.langchain4j.model.mistralai.internal.api.MistralAiResponseFormat;
import dev.langchain4j.model.mistralai.internal.api.MistralAiResponseFormatType;
import dev.langchain4j.model.mistralai.internal.api.MistralAiRole;
import dev.langchain4j.model.mistralai.internal.api.MistralAiTool;
import dev.langchain4j.model.mistralai.internal.api.MistralAiToolCall;
import dev.langchain4j.model.mistralai.internal.api.MistralAiToolType;
import dev.langchain4j.model.mistralai.internal.api.MistralAiUsage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MistralAiMapper {

    public static List<MistralAiChatMessage> toMistralAiMessages(List<ChatMessage> messages) {
        return messages.stream().map(MistralAiMapper::toMistralAiMessage).collect(toList());
    }

    static MistralAiChatMessage toMistralAiMessage(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return MistralAiChatMessage.builder()
                    .role(MistralAiRole.SYSTEM)
                    .content(((SystemMessage) message).text())
                    .build();
        }

        if (message instanceof AiMessage) {
            AiMessage aiMessage = (AiMessage) message;

            if (!aiMessage.hasToolExecutionRequests()) {
                return MistralAiChatMessage.builder()
                        .role(MistralAiRole.ASSISTANT)
                        .content(aiMessage.text())
                        .build();
            }

            List<MistralAiToolCall> toolCalls = aiMessage.toolExecutionRequests().stream()
                    .map(MistralAiMapper::toMistralAiToolCall)
                    .collect(toList());

            if (isNullOrBlank(aiMessage.text())) {
                return MistralAiChatMessage.builder()
                        .role(MistralAiRole.ASSISTANT)
                        .content((String) null)
                        .toolCalls(toolCalls)
                        .build();
            }

            return MistralAiChatMessage.builder()
                    .role(MistralAiRole.ASSISTANT)
                    .content(aiMessage.text())
                    .toolCalls(toolCalls)
                    .build();
        }

        if (message instanceof UserMessage userMessage) {

            if (userMessage.hasSingleText()) {
                return MistralAiChatMessage.builder()
                        .role(MistralAiRole.USER)
                        .content(userMessage.singleText())
                        .build();
            } else {
                return MistralAiChatMessage.builder()
                        .role(MistralAiRole.USER)
                        .content(userMessage.contents().stream()
                                .map(MistralAiMapper::toMistralAiContent)
                                .toList())
                        .name(userMessage.name())
                        .build();
            }
        }

        if (message instanceof ToolExecutionResultMessage) {
            return MistralAiChatMessage.builder()
                    .role(MistralAiRole.TOOL)
                    .toolCallId(((ToolExecutionResultMessage) message).id())
                    .name(((ToolExecutionResultMessage) message).toolName())
                    .content(((ToolExecutionResultMessage) message).text())
                    .build();
        }

        throw new IllegalArgumentException("Unknown message type: " + message.type());
    }

    private static MistralAiChatMessageContent toMistralAiContent(Content content) {
        if (content instanceof TextContent textContent) {
            return toMistralAiContent(textContent);
        } else if (content instanceof ImageContent imageContent) {
            return toMistralAiContent(imageContent);
        } else {
            throw illegalArgument("Unsupported content type: " + content);
        }
    }

    private static MistralAiChatMessageContent.TextContent toMistralAiContent(TextContent content) {
        return MistralAiChatMessageContent.TextContent.from(content.text());
    }

    private static MistralAiChatMessageContent.ImageUrlContent toMistralAiContent(ImageContent content) {
        final Image image = content.image();
        if (image.base64Data() == null) {
            return ImageUrlContent.from(image.url().toString());
        }
        return ImageUrlContent.from(image.mimeType(), image.base64Data());
    }

    static MistralAiToolCall toMistralAiToolCall(ToolExecutionRequest toolExecutionRequest) {
        return MistralAiToolCall.builder()
                .id(toolExecutionRequest.id())
                .function(MistralAiFunctionCall.builder()
                        .name(toolExecutionRequest.name())
                        .arguments(toolExecutionRequest.arguments())
                        .build())
                .build();
    }

    public static TokenUsage tokenUsageFrom(MistralAiUsage mistralAiUsage) {
        if (mistralAiUsage == null) {
            return null;
        }
        return new TokenUsage(
                mistralAiUsage.getPromptTokens(),
                mistralAiUsage.getCompletionTokens(),
                mistralAiUsage.getTotalTokens());
    }

    public static FinishReason finishReasonFrom(String mistralAiFinishReason) {
        if (mistralAiFinishReason == null) {
            return null;
        }
        switch (mistralAiFinishReason) {
            case "stop":
                return STOP;
            case "length":
                return LENGTH;
            case "tool_calls":
                return TOOL_EXECUTION;
            case "content_filter":
                return CONTENT_FILTER;
            case "model_length":
            default:
                return null;
        }
    }

    public static AiMessage aiMessageFrom(MistralAiChatCompletionResponse response) {
        MistralAiChatMessage aiMistralMessage = response.getChoices().get(0).getMessage();
        List<MistralAiToolCall> toolCalls = aiMistralMessage.getToolCalls();
        if (!isNullOrEmpty(toolCalls)) {
            return AiMessage.from(toToolExecutionRequests(toolCalls));
        }

        List<MistralAiChatMessageContent> contents = aiMistralMessage.getContents();
        if (contents != null && !contents.isEmpty()) {

            String text = contents.stream()
                    .map(content -> {
                        if (content instanceof MistralAiChatMessageContent.TextContent textContent) {
                            return textContent.getText();
                        }
                        if (content instanceof DocumentUrlContent documentUrlContent) {
                            return documentUrlContent.getDocumentUrl();
                        }
                        if (content instanceof ReferenceContent referenceContent) {
                            return referenceContent.getReferenceIds().stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(","));
                        }
                        if (content instanceof ImageUrlContent imageUrlContent) {
                            return imageUrlContent.getImageUrl();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            return AiMessage.from(text);
        }
        return AiMessage.from();
    }

    public static List<ToolExecutionRequest> toToolExecutionRequests(List<MistralAiToolCall> mistralAiToolCalls) {
        return mistralAiToolCalls.stream()
                .filter(toolCall -> toolCall.getType() == MistralAiToolType.FUNCTION)
                .map(MistralAiMapper::toToolExecutionRequest)
                .collect(toList());
    }

    public static ToolExecutionRequest toToolExecutionRequest(MistralAiToolCall mistralAiToolCall) {
        return ToolExecutionRequest.builder()
                .id(mistralAiToolCall.getId())
                .name(mistralAiToolCall.getFunction().getName())
                .arguments(mistralAiToolCall.getFunction().getArguments())
                .build();
    }

    public static List<MistralAiTool> toMistralAiTools(List<ToolSpecification> toolSpecifications) {
        return toolSpecifications.stream().map(MistralAiMapper::toMistralAiTool).collect(toList());
    }

    static MistralAiTool toMistralAiTool(ToolSpecification toolSpecification) {
        MistralAiFunction function = MistralAiFunction.builder()
                .name(toolSpecification.name())
                .description(toolSpecification.description())
                .parameters(toMistralAiParameters(toolSpecification))
                .build();
        return MistralAiTool.from(function);
    }

    static MistralAiParameters toMistralAiParameters(ToolSpecification toolSpecification) {
        if (toolSpecification.parameters() != null) {
            JsonObjectSchema parameters = toolSpecification.parameters();
            return MistralAiParameters.builder()
                    .properties(toMap(parameters.properties()))
                    .required(parameters.required())
                    .build();
        } else {
            return MistralAiParameters.builder().build();
        }
    }

    public static MistralAiResponseFormat toMistralAiResponseFormat(
            ResponseFormat responseFormat, ResponseFormat fallbackFormat) {
        if (responseFormat == null) {
            if (fallbackFormat == null) {
                return null;
            }
            responseFormat = fallbackFormat;
        }
        return switch (responseFormat.type()) {
            case TEXT -> MistralAiResponseFormat.fromType(MistralAiResponseFormatType.TEXT);
            case JSON ->
                responseFormat.jsonSchema() != null
                        ? MistralAiResponseFormat.fromSchema(responseFormat.jsonSchema())
                        : MistralAiResponseFormat.fromType(MistralAiResponseFormatType.JSON_OBJECT);
        };
    }
}
