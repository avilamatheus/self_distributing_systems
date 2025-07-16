package self.distributing.systems.containermanager.module.docker.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeploymentDTO {

	private String location;
	private int numberOfContainers;

}
