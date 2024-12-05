package self.distributing.systems.containermanager.module.docker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.HostConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import self.distributing.systems.containermanager.module.docker.controller.dto.listcontainer.ContainerDTO;
import self.distributing.systems.containermanager.module.docker.controller.dto.listcontainer.ContainerPortDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class DockerService {

	@Autowired
	private DockerClient dockerClient;

	private List<Container> containerList(boolean showStoppedContainers, String containerName) {
		List<String> statusFilter = new ArrayList<>();
		statusFilter.add("running");
		if (showStoppedContainers) {
			statusFilter.add("exited");
		}

		// Configura o comando e adiciona filtros de forma condicional
		var listCommand = dockerClient.listContainersCmd().withStatusFilter(statusFilter);
		if (containerName != null && !containerName.isEmpty()) {
			listCommand = listCommand.withNameFilter(List.of(containerName));
		}

		return listCommand.exec();
	}

	private ContainerDTO toContainerDTO(Container container, boolean detailed) {
		List<ContainerPortDTO> portsList = new ArrayList<>();
		if (detailed) {
			for (ContainerPort containerPort : container.getPorts()) {
				portsList.add(new ContainerPortDTO(
						containerPort.getIp(),
						containerPort.getPublicPort(),
						containerPort.getPrivatePort()
				));
			}
		}

		return new ContainerDTO(
				container.getId(),
				Arrays.stream(container.getNames()).toList().get(0).substring(1),
				detailed ? container.getImage() : null,
				detailed ? container.getState() : null,
				detailed ? portsList : null,
				detailed ? Collections.singletonList(container.getCommand()) : null
		);
	}

	public List<ContainerDTO> simpleListContainers(String containerName) {
		List<ContainerDTO> response = new ArrayList<>();
		for (Container container : containerList(false, containerName)) {
			response.add(toContainerDTO(container, false));
		}
		return response;
	}

	public List<ContainerDTO> detailedListContainers(String containerName) {
		List<ContainerDTO> response = new ArrayList<>();
		for (Container container : containerList(true, containerName)) {
			response.add(toContainerDTO(container, true));
		}
		return response;
	}


	public List<ContainerDTO> startContainers(int numberOfContainers, String containerName, String cmd) throws InterruptedException, DockerException {
		List<ContainerDTO> requestedContainers = requestedContainers(numberOfContainers, containerName, cmd);
		List<ContainerDTO> currentContainers = detailedListContainers(containerName);

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

		return this.simpleListContainers(containerName); // Lista os containers
	}

	public void stopAndRemoveContainers() {
		List<ContainerDTO> containers = this.detailedListContainers(null);
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
					.withTty(true)
					.withStdinOpen(true)
					.withCmd(containerDTO.cmd())
					.withHostConfig(
							HostConfig.newHostConfig()
									.withNetworkMode("sds_network")
					)
					.exec();

			// Inicia o container
			dockerClient.startContainerCmd(container.getId()).exec();
		}
	}

	private List<ContainerDTO> requestedContainers(int numberOfContainers, String containerName, String cmd) {
		List<String> cmdInput = splitStringBySpaces(cmd);
		List<ContainerDTO> listOfRequestedContainers = new ArrayList<>();

		for (int i = 1; i <= numberOfContainers; i++) {
			ContainerDTO requestedContainer = new ContainerDTO(null, containerName + i, null, null, null, cmdInput);
			listOfRequestedContainers.add(requestedContainer);
		}

		return listOfRequestedContainers;
	}

	private List<String> splitStringBySpaces(String input) {
		if (input == null || input.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.stream(input.split("\\s+"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
