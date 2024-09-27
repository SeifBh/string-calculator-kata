import com.openai.services.AIServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author madhankumar
 */
@RestController
@RequestMapping("/api/v1")
public class SpringAIController {

	@Autowired
	AIServiceHelper aiService;

	/**
	 * Handles a question-answer request by providing a response generated by the AI service.
	 *
	 * @param question The question asked by the user.
	 * @return The response generated by the AI service.
	 */
	@GetMapping("/qa")
	public String chat(@RequestParam String question) {
		return aiService.chat(question);
	}

	/**
	 * Generates a document based on the provided topic using the AI service.
	 *
	 * @param topic The topic for which the document needs to be generated.
	 * @return A string representing the generated document.
	 */
	@GetMapping("/docuGenAI")
	public String generateDocument(@RequestParam String topic) {
		String me = handleError();
		return aiService.generateDocument(topic);
	}

	public String handleError(){
		return "";
	}

}
