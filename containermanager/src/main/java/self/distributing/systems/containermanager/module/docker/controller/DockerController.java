package self.distributing.systems.containermanager.module.docker.controller;

import java.util.List;

import org.springframework.http.MediaType;
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

	@GetMapping(value = "/list-containers/{containerName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> listContainers(@PathVariable String containerName) {
		try {
			List<String> ips = dockerService.getServiceReplicaNames(containerName);
			return ResponseEntity.ok().body(ips);
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao listar containers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}

	@PostMapping("/start-containers/{location}/{containerName}/{numberOfContainers}")
	public ResponseEntity<?> startContainers(
			@PathVariable String location,
			@PathVariable String containerName,
			@PathVariable int numberOfContainers,
			HttpServletRequest request) {
		try {
			String cmd = request.getHeader("cmd");
			dockerService.startOrUpdateService(containerName, cmd, numberOfContainers, location);
			return ResponseEntity.ok().build();
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao startar containers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}

	@GetMapping("/{serviceName}/ips")
	public ResponseEntity<List<String>> getServiceIps(@PathVariable String serviceName) {
		List<String> ips = dockerService.getServiceReplicaIPs(serviceName);
		return ResponseEntity.ok().body(ips);
	}

	@DeleteMapping("/{serviceName}")
	public ResponseEntity<?> removeService(@PathVariable String serviceName) {
		try {
			dockerService.removeService(serviceName);
			return ResponseEntity.ok().build();
		} catch (DockerException e) {
			ErrorDTO errorDTO = new ErrorDTO("Erro ao remover servico: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
		}
	}
}
