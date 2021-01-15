package de.vitagroup.num.service;

import de.vitagroup.num.domain.Comment;
import de.vitagroup.num.domain.Study;
import de.vitagroup.num.domain.admin.UserDetails;
import de.vitagroup.num.domain.repository.CommentRepository;
import de.vitagroup.num.web.exception.BadRequestException;
import de.vitagroup.num.web.exception.ForbiddenException;
import de.vitagroup.num.web.exception.ResourceNotFound;
import de.vitagroup.num.web.exception.SystemException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserDetailsService userDetailsService;
  private final StudyService studyService;

  public List<Comment> getComments(Long studyId) {
    studyService.getStudyById(studyId).orElseThrow(ResourceNotFound::new);
    return commentRepository.findByStudyId(studyId);
  }

  public Comment createComment(Comment comment, Long studyId, String loggedInUserId) {
    UserDetails author =
        userDetailsService.getUserDetailsById(loggedInUserId).orElseThrow(SystemException::new);

    if (author.isNotApproved()) {
      throw new ForbiddenException("Cannot access this resource. Logged in user is not approved.");
    }

    Study study = studyService.getStudyById(studyId).orElseThrow(ResourceNotFound::new);

    comment.setStudy(study);
    comment.setAuthor(author);
    comment.setCreateDate(OffsetDateTime.now());
    return commentRepository.save(comment);
  }

  public Comment updateComment(
      Comment comment, Long commentId, String loggedInUserId, Long studyId) {

    validateLoggedInUser(loggedInUserId);

    studyService.getStudyById(studyId).orElseThrow(ResourceNotFound::new);

    Comment commentToEdit =
        commentRepository.findById(commentId).orElseThrow(ResourceNotFound::new);

    if (commentToEdit.hasEmptyOrDifferentAuthor(loggedInUserId)) {
      throw new ForbiddenException(
          String.format(
              "%s: %s %s.",
              "Comment edit for comment with id",
              commentId,
              "not allowed. Comment has different athor"));
    }

    commentToEdit.setText(comment.getText());
    return commentRepository.save(commentToEdit);
  }

  public void deleteComment(Long commentId, Long studyId, String loggedInUserId) {
    validateLoggedInUser(loggedInUserId);
    studyService.getStudyById(studyId).orElseThrow(ResourceNotFound::new);
    Comment comment = commentRepository.findById(commentId).orElseThrow(ResourceNotFound::new);

    if (comment.hasEmptyOrDifferentAuthor(loggedInUserId)) {
      throw new ForbiddenException("Cannot delete comment: " + commentId);
    }

    try {
      commentRepository.deleteById(commentId);
    } catch (EmptyResultDataAccessException e) {
      throw new BadRequestException(String.format("%s: %s", "Invalid commentId id", commentId));
    }
  }

  private void validateLoggedInUser(String loggedInUserId) {
    UserDetails author =
        userDetailsService.getUserDetailsById(loggedInUserId).orElseThrow(SystemException::new);

    if (author.isNotApproved()) {
      throw new ForbiddenException("Cannot access this resource. Logged in user is not approved.");
    }
  }
}
