package self.distributing.systems.containermanager.module.docker.controller.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartContainerDTO {

	private String containerName;
	private String cmd;
	private List<DeploymentDTO> deployments;


}
