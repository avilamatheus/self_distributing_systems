package self.distributing.systems.containermanager.module.docker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.model.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@org.springframework.stereotype.Service
public class DockerService {

	@Autowired
	private DockerClient dockerClient;

	public void createSwarmService(String serviceName, List<String> cmd, int replicas, String targetLocation) {
		ServiceSpec serviceSpec = new ServiceSpec()
				.withName(serviceName)
				.withTaskTemplate(
						new TaskSpec()
								.withContainerSpec(new ContainerSpec()
										.withImage("avilamatheus/dana-sds:latest")
										.withCommand(cmd)
										.withTty(true)
										.withOpenStdin(true)
								)
								.withPlacement(new ServicePlacement()
										.withConstraints(List.of("node.labels.location==" + targetLocation))
								)
				)
				.withMode(new ServiceModeConfig()
						.withReplicated(new ServiceReplicatedModeOptions()
								.withReplicas(replicas))
				)
				.withNetworks(List.of(new NetworkAttachmentConfig().withTarget("sds_network")));

		dockerClient.createServiceCmd(serviceSpec).exec();
	}

	public List<String> getServiceReplicaIPs(String serviceName) {
		List<String> ipList = new ArrayList<>();

		dockerClient.listContainersCmd()
				.withStatusFilter(List.of("running"))
				.withLabelFilter(Map.of("com.docker.swarm.service.name", serviceName))
				.exec()
				.forEach(container -> {
					InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
					ContainerNetwork network = inspect.getNetworkSettings().getNetworks().get("sds_network");
					if (network != null && network.getIpAddress() != null) {
						ipList.add(network.getIpAddress());
					}
				});

		return ipList;
	}

	public List<Service> listServicesByName(String serviceName) {
		return dockerClient.listServicesCmd()
				.withNameFilter(Collections.singletonList(serviceName))
				.exec();
	}

	public void removeService(String serviceName) {
		listServicesByName(serviceName).forEach(service ->
				dockerClient.removeServiceCmd(service.getId()).exec()
		);
	}

	public void updateServiceReplicas(String serviceName, int replicas) {
		List<Service> services = listServicesByName(serviceName);
		if (!services.isEmpty()) {
			Service service = services.get(0);
			ServiceSpec currentSpec = service.getSpec();

			ServiceSpec updatedSpec = new ServiceSpec()
					.withName(currentSpec.getName())
					.withTaskTemplate(currentSpec.getTaskTemplate())
					.withNetworks(currentSpec.getNetworks())
					.withEndpointSpec(currentSpec.getEndpointSpec())
					.withMode(new ServiceModeConfig()
							.withReplicated(new ServiceReplicatedModeOptions()
									.withReplicas(replicas)));

			dockerClient.updateServiceCmd(service.getId(), updatedSpec)
					.withVersion(service.getVersion().getIndex())
					.exec();
		}
	}

	public void startOrUpdateService(String serviceName, String cmd, int replicas, String newLocation) {
		List<String> commandList = splitStringBySpaces(cmd);
		List<Service> existing = listServicesByName(serviceName);

		if (existing.isEmpty()) {
			createSwarmService(serviceName, commandList, replicas, newLocation);
			return;
		}

		Service service = existing.get(0);
		List<String> currentConstraints = Optional.ofNullable(service.getSpec().getTaskTemplate().getPlacement())
				.map(ServicePlacement::getConstraints)
				.orElse(Collections.emptyList());

		String desiredConstraint = "node.labels.location==" + newLocation;

		boolean constraintChanged = currentConstraints.stream().noneMatch(desiredConstraint::equals);

		if (constraintChanged) {
			removeService(serviceName);
			createSwarmService(serviceName, commandList, replicas, newLocation);
		} else {
			updateServiceReplicas(serviceName, replicas);
		}
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

	public List<String> getServiceReplicaNames(String serviceName) {
		List<String> names = new ArrayList<>();

		List<Task> tasks = dockerClient.listTasksCmd()
				.withServiceFilter(serviceName)
				.withStateFilter(TaskState.RUNNING)
				.exec();

		for (Task task : tasks) {
			if (task.getStatus().getState() == TaskState.RUNNING) {
				String name = serviceName + "." + task.getSlot() + "." + task.getId();
				names.add(name);
			}
		}

		return names;
	}

}
