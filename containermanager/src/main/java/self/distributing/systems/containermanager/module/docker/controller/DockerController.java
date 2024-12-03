package self.distributing.systems.containermanager.module.docker.controller;

import com.github.dockerjava.api.exception.DockerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import self.distributing.systems.containermanager.module.docker.controller.dto.error.ErrorDTO;
import self.distributing.systems.containermanager.module.docker.service.DockerService;

@RestController
@RequestMapping("/docker")
public class DockerController {

    @Autowired
    private DockerService dockerService;

    @GetMapping("/list-containers")
    public ResponseEntity<?> listContainers() {
        try {
            return ResponseEntity.ok(dockerService.simpleListContainers());
        } catch (DockerException e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro ao listar containers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
        }
    }

	@GetMapping("/list-containers-detailed")
	public ResponseEntity<?> listContainersDetailed() {
		try {
			return ResponseEntity.ok(dockerService.detailedListContainers());
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao listar containers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}

	@PostMapping("/start-containers/{numberOfContainers}")
    public ResponseEntity<?> startContainers(@PathVariable int numberOfContainers) {
		try {
			return ResponseEntity.ok(dockerService.startContainers(numberOfContainers));
		} catch (InterruptedException | DockerException e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro ao startar containers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}

	@DeleteMapping("/remove-containers")
	public ResponseEntity<?> removeContainers() {
		try {
			dockerService.stopAndRemoveContainers();
			return ResponseEntity.ok().build();
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao remover containers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}
}
