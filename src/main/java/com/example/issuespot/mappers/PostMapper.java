package com.example.issuespot.mappers;
import com.example.issuespot.domain.dtos.PostWithProfileDto;
import com.example.issuespot.domain.dtos.ProfileInfoDto;
import com.example.issuespot.domain.entities.Post;
import com.example.issuespot.domain.entities.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.locationtech.jts.geom.Point;
import com.example.issuespot.domain.dtos.CoordinatesDto;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "profiles", source = "profile")
    @Mapping(target = "coordinates", source = "coordinates", qualifiedByName = "pointToDto")
    PostWithProfileDto toDto(Post post, Profile profile);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "imageUrl", source = "imageUrl")
    ProfileInfoDto toProfileInfoDto(Profile profile);

    @Named("pointToDto")
    default CoordinatesDto pointToDto(Point point) {
        if (point == null) return null;
        return new CoordinatesDto(point.getY(), point.getX());
    }
}
