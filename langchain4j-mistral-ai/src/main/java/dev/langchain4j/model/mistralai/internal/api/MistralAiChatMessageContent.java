package dev.langchain4j.model.mistralai.internal.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;
import java.util.List;

public class MistralAiChatMessageContent {

    public enum ContentType {
        @JsonProperty("text")
        TEXT,
        @JsonProperty("document_url")
        DOCUMENT_URL,
        @JsonProperty("image_url")
        IMAGE_URL,
        @JsonProperty("reference")
        REFERENCE;

        ContentType() {}

        /**
         * Returns the string representation in lowercase of the response format type.
         */
        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private final ContentType type;

    public ContentType getType() {
        return type;
    }

    public MistralAiChatMessageContent(final ContentType type) {
        this.type = type;
    }

    public static class TextContent extends MistralAiChatMessageContent {

        private final String text;

        public TextContent(final String text) {
            super(ContentType.TEXT);
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public static TextContent from(final String text) {
            return new TextContent(text);
        }
    }

    public static class DocumentUrlContent extends MistralAiChatMessageContent {

        /**
         * string (Document Url)
         */
        @JsonProperty("document_url")
        private String documentUrl;
        /**
         * Document Name (string) or Document Name (null) (Document Name)
         * The filename of the document
         */
        @JsonProperty("document_name")
        private String documentName;

        public DocumentUrlContent(final String documentUrl, final String documentName) {
            super(ContentType.DOCUMENT_URL);
            this.documentUrl = documentUrl;
            this.documentName = documentName;
        }

        public String getDocumentUrl() {
            return documentUrl;
        }

        public String getDocumentName() {
            return documentName;
        }
    }

    // image_url : "data:{mimetype};base64,{base64-encoded-image-data}"
    public static class ImageUrlContent extends MistralAiChatMessageContent {

        /**
         * ImageURL (object) or Image Url (string) (Image Url)
         */
        @JsonProperty("image_url")
        private String imageUrl;

        public ImageUrlContent(final String imageUrl) {
            super(ContentType.IMAGE_URL);
            this.imageUrl = imageUrl;
        }

        /**
         * @param mimetype     The image mime type. ex: image/png, image/jpg, ...
         * @param imageRawData The image raw content as read from disk or elsewhere.
         */
        public ImageUrlContent(String mimetype, final byte[] imageRawData) {
            this(mimetype, Base64.getEncoder().encodeToString(imageRawData));
        }

        /**
         * @param mimetype   The image mime type. ex: image/png, image/jpg, ...
         * @param base64Data The image content encoded as base64 data.
         */
        public ImageUrlContent(String mimetype, final String base64Data) {
            this(String.format("data:%s;base64,%s", mimetype, base64Data));
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public static ImageUrlContent from(final String imageUrl) {
            return new ImageUrlContent(imageUrl);
        }

        public static ImageUrlContent from(String mimetype, final byte[] imageRawData) {
            return new ImageUrlContent(mimetype, imageRawData);
        }

        public static ImageUrlContent from(String mimetype, final String base64Data) {
            return new ImageUrlContent(mimetype, base64Data);
        }
    }

    public static class ReferenceContent extends MistralAiChatMessageContent {

        /**
         * Array of integers (Reference Ids)
         */
        @JsonProperty("reference_ids")
        private List<Integer> referenceIds;

        public ReferenceContent(final List<Integer> referenceIds) {
            super(ContentType.REFERENCE);
            this.referenceIds = referenceIds;
        }

        public List<Integer> getReferenceIds() {
            return referenceIds;
        }
    }
}
