package self.distributing.systems.containermanager.module.docker.controller.dto.listcontainer;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContainerDTO(
        String id,
        String name,
        String image,
        String state,
        List<ContainerPortDTO> ports,
		List<String> cmd
) {

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ContainerDTO that = (ContainerDTO) o;
		return Objects.equals(name, that.name);
	}

	@Override public int hashCode() {
		return Objects.hashCode(name);
	}
}
