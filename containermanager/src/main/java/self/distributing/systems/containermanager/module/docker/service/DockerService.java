package self.distributing.systems.containermanager.module.docker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.ExposedPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import self.distributing.systems.containermanager.module.docker.controller.dto.listcontainer.ContainerDTO;
import self.distributing.systems.containermanager.module.docker.controller.dto.listcontainer.ContainerPortDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

@Service
public class DockerService {

	@Autowired
	private DockerClient dockerClient;

	private final static String CONTAINER_NAME = "remote-dist";

	private List<Container> containerList(boolean showStoppedContainers) {
		Map<String, List<String>> filters = new HashMap<>();
		filters.put("name", List.of("remote-dist"));

		List<String> statusFilter = new ArrayList<>();
		statusFilter.add("running");
		if(showStoppedContainers) {statusFilter.add("exited");}

		return dockerClient.listContainersCmd()
				.withNameFilter(List.of("remote-dist"))
				.withStatusFilter(statusFilter)
				.exec();
	}

	public List<ContainerDTO> simpleListContainers() {
		List<ContainerDTO> response = new ArrayList<>();
		List<Container> runningContainers = this.containerList(false);

		for (Container container : runningContainers) {
			ContainerDTO containerDTO = new ContainerDTO(
					null,
					Arrays.stream(container.getNames()).toList().get(0).substring(1),
					null,
					null,
					null
			);
			response.add(containerDTO);
		}
		return response;
	}

	public List<ContainerDTO> detailedListContainers() {
		List<ContainerDTO> response = new ArrayList<>();

		for (Container container : containerList(true)) {

			List<ContainerPortDTO> portsList = new ArrayList<>();
			for (ContainerPort containerPort : container.getPorts()) {
				ContainerPortDTO portDTO = new ContainerPortDTO(
						containerPort.getIp(),
						containerPort.getPublicPort(),
						containerPort.getPrivatePort()
				);
				portsList.add(portDTO);
			}

			ContainerDTO containerDTO = new ContainerDTO(
					container.getId(),
					Arrays.stream(container.getNames()).toList().get(0).substring(1),
					container.getImage(),
					container.getState(),
					portsList
			);

			response.add(containerDTO);
		}
		return response;
	}

	public List<ContainerDTO> startContainers(int numberOfContainers) throws InterruptedException, DockerException {
		List<ContainerDTO> requestedContainers = requestedContainers(numberOfContainers);
		List<ContainerDTO> currentContainers = detailedListContainers();

		List<ContainerDTO> containersToBeStopped = currentContainers.stream()
				.filter(container -> !requestedContainers.contains(container) && !container.state().equals("exited"))
				.toList();
		requestedContainers.removeAll(containersToBeStopped);

		this.stopContainers(containersToBeStopped);

		requestedContainers.removeAll(
				currentContainers
						.stream()
						.filter(current -> current.state() != null
										&& current.state().equals("running")
						).toList()
		);

		List<ContainerDTO> containersToBeCreated = requestedContainers.stream()
				.filter(container -> !currentContainers.contains(container))
				.toList();
		requestedContainers.removeAll(containersToBeCreated);
		this.createAndStartContainers(containersToBeCreated);

		List<ContainerDTO> stoppedContainersToBeStarted = currentContainers.stream()
				.filter(requested -> requestedContainers.stream()
						.anyMatch(current -> current.name().equals(requested.name())))
				.toList();
		requestedContainers.removeAll(stoppedContainersToBeStarted);
		this.startStoppedContainers(stoppedContainersToBeStarted);

		return this.simpleListContainers(); // Lista os containers
	}

	public void stopAndRemoveContainers() {
		List<ContainerDTO> containers = this.detailedListContainers();
		for (ContainerDTO container : containers) {
			dockerClient.removeContainerCmd(container.id()).withForce(true).exec();
		}
	}

	private void startStoppedContainers(List<ContainerDTO> containers) {
		for (ContainerDTO containerDTO : containers) {
			dockerClient.startContainerCmd(containerDTO.id()).exec();
		}
	}

	private void stopContainers(List<ContainerDTO> containers) {
		for (ContainerDTO containerDTO : containers) {
			dockerClient.killContainerCmd(containerDTO.id()).exec();
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}

	private void createAndStartContainers(List<ContainerDTO> containers) {
		for (ContainerDTO containerDTO : containers) {
			// Cria o container
			CreateContainerResponse container = dockerClient.createContainerCmd("dana")
					.withName(containerDTO.name())
					// desnecessario, jã que estâo na mesma rede:
					//.withExposedPorts(ExposedPort.tcp(2010), ExposedPort.tcp(8081))
					.withTty(true)
					.withStdinOpen(true)
					.withNetworkMode("sds_network")
					.withCmd("dana", "-sp", "../readn", "RemoteDist.o")
					.exec();

			// Inicia o container
			dockerClient.startContainerCmd(container.getId()).exec();
		}
	}

	private List<ContainerDTO> requestedContainers(int numberOfContainers) {
		List<ContainerDTO> listOfRequestedContainers = new ArrayList<>();

		for (int i = 1; i <= numberOfContainers; i++) {
			ContainerDTO requestedContainer = new ContainerDTO(null, CONTAINER_NAME + i, null, null, null);
			listOfRequestedContainers.add(requestedContainer);
		}

		return listOfRequestedContainers;
	}

}
