import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReviewMyCodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewMyCodeApplication.class, args);
	}

	@PostConstruct
	public void init() {
		System.out.println("Hello World!");
	}
}
