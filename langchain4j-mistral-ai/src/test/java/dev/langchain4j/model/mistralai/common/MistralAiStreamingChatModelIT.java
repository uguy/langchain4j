package dev.langchain4j.model.mistralai.common;

import static dev.langchain4j.model.mistralai.MistralAiChatModelName.PIXTRAL;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.common.AbstractStreamingChatModelIT;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import java.util.List;

class MistralAiStreamingChatModelIT extends AbstractStreamingChatModelIT {

    static final StreamingChatLanguageModel MISTRAL_STREAMING_CHAT_MODEL = MistralAiStreamingChatModel.builder()
            .apiKey(System.getenv("MISTRAL_AI_API_KEY"))
            .modelName(PIXTRAL)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .build();

    @Override
    protected List<StreamingChatLanguageModel> models() {
        return List.of(MISTRAL_STREAMING_CHAT_MODEL);
    }

    @Override
    protected boolean supportsDefaultRequestParameters() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsModelNameParameter() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsMaxOutputTokensParameter() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsStopSequencesParameter() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsToolChoiceRequiredWithMultipleTools() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsJsonResponseFormat() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsJsonResponseFormatWithSchema() {
        return false; // TODO implement
    }

    @Override
    protected boolean assertResponseId() {
        return false; // TODO implement
    }

    @Override
    protected boolean assertResponseModel() {
        return false; // TODO implement
    }
}
