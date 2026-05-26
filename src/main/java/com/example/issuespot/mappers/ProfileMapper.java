package com.example.issuespot.mappers;
import com.example.issuespot.domain.dtos.ProfileDto;
import com.example.issuespot.domain.entities.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileDto toDto(Profile profile);
}
