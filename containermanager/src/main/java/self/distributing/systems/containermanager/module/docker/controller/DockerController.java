package self.distributing.systems.containermanager.module.docker.controller;

import com.github.dockerjava.api.exception.DockerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import self.distributing.systems.containermanager.module.docker.controller.dto.error.ErrorDTO;
import self.distributing.systems.containermanager.module.docker.service.DockerService;

@RestController
@RequestMapping("/docker")
public class DockerController {

    @Autowired
    private DockerService dockerService;

	@GetMapping("/list-containers/{containerName}")
	public ResponseEntity<?> listContainers(@PathVariable(required = false) String containerName) {
		try {
			return ResponseEntity.ok(dockerService.simpleListContainers(containerName));
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao listar containers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}

	// Outra rota para detalhamento
	@GetMapping("/list-containers/detailed/{containerName}")
	public ResponseEntity<?> detailedListContainers(@PathVariable(required = false) String containerName) {
		try {
			return ResponseEntity.ok(dockerService.detailedListContainers(containerName));
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao listar containers detalhados: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}

	@PostMapping("/start-containers/{containerName}/{numberOfContainers}")
	public ResponseEntity<?> startContainers(
			@PathVariable int numberOfContainers,
			@PathVariable String containerName,
			HttpServletRequest request) {
		try {
			String cmd = request.getHeader("cmd");
			return ResponseEntity.ok(dockerService.startContainers(numberOfContainers, containerName, cmd));
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
