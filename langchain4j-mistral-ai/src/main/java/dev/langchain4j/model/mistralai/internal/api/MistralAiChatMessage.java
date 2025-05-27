package dev.langchain4j.model.mistralai.internal.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dev.langchain4j.model.mistralai.internal.api.MistralAiChatMessageContent.TextContent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
@JsonDeserialize(builder = MistralAiChatMessage.MistralAiChatMessageBuilder.class)
public class MistralAiChatMessage {

    private MistralAiRole role;
    @JsonProperty("content")
    private List<MistralAiChatMessageContent> contents;
    private String name;
    private List<MistralAiToolCall> toolCalls;
    private String toolCallId;

    private MistralAiChatMessage(MistralAiChatMessageBuilder builder) {
        this.role = builder.role;
        this.contents = builder.contents;
        this.name = builder.name;
        this.toolCalls = builder.toolCalls;
        this.toolCallId = builder.toolCallId;
    }

    public MistralAiRole getRole() {
        return this.role;
    }

    @JsonIgnore
    @Deprecated
    /** User contents field instead */
    public String getContent() {
        // TODO: implement this.contents.stream().findFirst()
        return null;
    }

    public List<MistralAiChatMessageContent> getContents() {
        return this.contents;
    }

    public String getName() {
        return this.name;
    }

    public String getToolCallId() {
        return this.toolCallId;
    }

    public List<MistralAiToolCall> getToolCalls() {
        return this.toolCalls;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.role);
        hash = 97 * hash + Objects.hashCode(this.contents);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.toolCallId);
        hash = 97 * hash + Objects.hashCode(this.toolCalls);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MistralAiChatMessage other = (MistralAiChatMessage) obj;
        return Objects.equals(this.contents, other.contents)
                && Objects.equals(this.name, other.name)
                && this.role == other.role
                && Objects.equals(this.toolCallId, other.toolCallId)
                && Objects.equals(this.toolCalls, other.toolCalls);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "MistralAiChatMessage [", "]")
                .add("role=" + this.getRole())
                .add("contents=" + this.getContents())
                .add("name=" + this.getName())
                .add("toolCallId=" + this.getToolCallId())
                .add("toolCalls=" + this.getToolCalls())
                .toString();
    }

    public static MistralAiChatMessageBuilder builder() {
        return new MistralAiChatMessageBuilder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(SnakeCaseStrategy.class)
    public static class MistralAiChatMessageBuilder {

        private MistralAiRole role;
        private List<MistralAiChatMessageContent> contents;
        private String name;
        private String toolCallId;
        private List<MistralAiToolCall> toolCalls;

        private MistralAiChatMessageBuilder() {}

        /**
         * @return {@code this}.
         */
        public MistralAiChatMessageBuilder role(MistralAiRole role) {
            this.role = role;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MistralAiChatMessageBuilder content(String content) {
            if (content != null) {
                if (this.contents == null) {
                    this.contents = new ArrayList<>();
                }
                this.contents.add(TextContent.from(content));
            }
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MistralAiChatMessageBuilder content(List<MistralAiChatMessageContent> contents) {
            this.contents = contents;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MistralAiChatMessageBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MistralAiChatMessageBuilder toolCallId(String toolCallId) {
            this.toolCallId = toolCallId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MistralAiChatMessageBuilder toolCalls(List<MistralAiToolCall> toolCalls) {
            this.toolCalls = toolCalls;
            return this;
        }

        public MistralAiChatMessage build() {
            return new MistralAiChatMessage(this);
        }
    }
}
