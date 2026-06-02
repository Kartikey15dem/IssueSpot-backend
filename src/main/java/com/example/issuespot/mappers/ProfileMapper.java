package com.example.issuespot.mappers;
import com.example.issuespot.domain.dtos.ProfileDto;
import com.example.issuespot.domain.entities.Profile;
import java.util.List;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mapping(target = "postByArea", source = "postByArea")
    ProfileDto toDto(Profile profile, List<Integer> postByArea);
}
