package de.vitagroup.num.attachment.domain.repository;

import de.vitagroup.num.attachment.domain.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepositoryJpa extends JpaRepository<Attachment, Long> {

    @Query("SELECT new Attachment (atc.id, atc.name, atc.description, atc.uploadDate) " +
            "FROM Attachment atc ")
    List<Attachment> getAttachments();
    @Query("SELECT new Attachment (atc.id, atc.name, atc.description, atc.uploadDate) " +
            "FROM Attachment atc " +
            "WHERE atc.projectId = :projectId")
    List<Attachment> findAttachmentsByProjectId(@Param("projectId") Long projectId);
}
