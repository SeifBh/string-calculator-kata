import lombok.*;

import java.util.List;

    /**
     * @author madhankumar
     */
    @Setter
    @Getter
    @ToString
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class ChatRequest {
        private String model;
        private List<?> messages;
        private int n;
        private double temperature;

        public boolean newMethod(String st1 ,String st2){
            if(st1 == st2){
                return true;
            }
            return false;
        }
        public String chat(String question){
            /* Initialize Variable */
            PromptTemplate promptTemplate = null;
            try {
                /* Create a prompt template using the question */
                promptTemplate = new PromptTemplate(question);
            }catch(Exception e) {
                log.error("error : "+e.getMessage());

            }
            /* Generate a response using the AI client and return the text of the generated response */
            return this.aiClient.generate(promptTemplate.create()).getGeneration().getText();
        }

        /**
         * Generates a document content for the specified topic using the AI client.
         *
         * @param topic The topic for which the document content needs to be generated.
         * @return The generated document content as a string.
         */
        public String generateDocument(String topic) {
            /* Initialize variable */
            PromptTemplate promptTemplate =null;
            try {

                /* Create a prompt template with place holders for the topic and document content instructions */
                promptTemplate = new PromptTemplate("""
					Generate document content for a {topic}.
					It should be at least two pages long and include comprehensive information covering all aspects of the topic,
					including background information, current trends or developments, relevant statistics or data, key concepts or
					theories, potential challenges, and future outlook. The document should be well-structured with clear headings
					and sub-headings, and it should provide an in-depth analysis that offers insights and engages the reader effectively.
					      """);

                /* Replace the placeholder {topic} with the actual topic provided */
                promptTemplate.add("topic", topic);
            }catch(Exception e) {
                log.error("error : "+e.getMessage());
            }

            /* Generate document content using the AI client and return the text of the generated content */
            return this.aiClient.generate(promptTemplate.create()).getGeneration().getText();
        }
    }
