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
    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "userId", source = "post.userId")
    @Mapping(target = "postLevel", source = "post.postLevel")
    @Mapping(target = "postText", source = "post.postText")
    @Mapping(target = "mediaType", source = "post.mediaType")
    @Mapping(target = "mediaUrls", source = "post.mediaUrls")
    @Mapping(target = "likes", source = "post.likes")
    @Mapping(target = "comments", source = "post.comments")
    @Mapping(target = "createdAt", source = "post.createdAt")
    @Mapping(target = "locality", source = "post.locality")
    @Mapping(target = "district", source = "post.district")
    @Mapping(target = "state", source = "post.state")
    @Mapping(target = "country", source = "post.country")
    @Mapping(target = "profiles", source = "profile")
    @Mapping(target = "coordinates", source = "post.coordinates", qualifiedByName = "pointToDto")
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
