package de.vitagroup.num.web.controller;

import de.vitagroup.num.domain.Roles;
import de.vitagroup.num.domain.admin.User;
import de.vitagroup.num.domain.dto.OrganizationDto;
import de.vitagroup.num.service.UserDetailsService;
import de.vitagroup.num.service.UserService;
import de.vitagroup.num.service.logger.AuditLog;
import de.vitagroup.num.web.config.Role;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admin/user", produces = "application/json")
@AllArgsConstructor
public class AdminController {

  private static final String SUCCESS_REPLY = "Success";
  private static final String EMAIL_CLAIM = "email";

  private final UserService userService;

  private final UserDetailsService userDetailsService;

  @AuditLog
  @DeleteMapping("/{userId}")
  @PreAuthorize(Role.SUPER_ADMIN)
  void deleteUser(@AuthenticationPrincipal @NotNull Jwt principal, @PathVariable String userId) {
    userService.deleteUser(userId, principal.getSubject());
  }

  @AuditLog
  @GetMapping("/{userId}")
  @ApiOperation(value = "Retrieves the information about the given user")
  public ResponseEntity<User> getUser(
      @AuthenticationPrincipal @NotNull Jwt principal, @NotNull @PathVariable String userId) {
    return ResponseEntity.ok(userService.getUserById(userId, true, principal.getSubject()));
  }

  @AuditLog
  @GetMapping("/{userId}/role")
  @ApiOperation(value = "Retrieves the roles of the given user")
  @PreAuthorize(Role.SUPER_ADMIN_OR_ORGANIZATION_ADMIN)
  public ResponseEntity<Set<de.vitagroup.num.domain.admin.Role>> getRolesOfUser(
      @AuthenticationPrincipal @NotNull Jwt principal, @NotNull @PathVariable String userId) {
    return ResponseEntity.ok(userService.getUserRoles(userId, principal.getSubject()));
  }

  @AuditLog
  @PostMapping("/{userId}/role")
  @ApiOperation(value = "Updates the users roles to the given set.")
  @PreAuthorize(Role.SUPER_ADMIN_OR_ORGANIZATION_ADMIN)
  public ResponseEntity<List<String>> updateRoles(
      @AuthenticationPrincipal @NotNull Jwt principal,
      @NotNull @PathVariable String userId,
      @NotNull @RequestBody List<String> roles) {

    return ResponseEntity.ok(
        userService.setUserRoles(
            userId, roles, principal.getSubject(), Roles.extractRoles(principal)));
  }

  @AuditLog
  @PostMapping("/{userId}/organization")
  @ApiOperation(value = "Sets the user's organization")
  @PreAuthorize(Role.SUPER_ADMIN)
  public ResponseEntity<String> setOrganization(
      @AuthenticationPrincipal @NotNull Jwt principal,
      @NotNull @PathVariable String userId,
      @NotNull @RequestBody OrganizationDto organization) {

    userDetailsService.setOrganization(principal.getSubject(), userId, organization.getId());
    return ResponseEntity.ok(SUCCESS_REPLY);
  }

  @AuditLog
  @PostMapping("/{userId}")
  @ApiOperation(value = "Creates user details")
  public ResponseEntity<String> createUserOnFirstLogin(
      @AuthenticationPrincipal @NotNull Jwt principal, @NotNull @PathVariable String userId) {

    userDetailsService.createUserDetails(userId, principal.getClaimAsString(EMAIL_CLAIM));
    return ResponseEntity.ok(SUCCESS_REPLY);
  }

  @AuditLog
  @PostMapping("/{userId}/approve")
  @ApiOperation(value = "Adds the given organization to the user")
  @PreAuthorize(Role.SUPER_ADMIN_OR_ORGANIZATION_ADMIN)
  public ResponseEntity<String> approveUser(
      @AuthenticationPrincipal @NotNull Jwt principal, @NotNull @PathVariable String userId) {
    userDetailsService.approveUser(principal.getSubject(), userId);
    return ResponseEntity.ok(SUCCESS_REPLY);
  }

  @AuditLog
  @GetMapping()
  @ApiOperation(value = "Retrieves a set of users that match the search string")
  @PreAuthorize(Role.SUPER_ADMIN_OR_ORGANIZATION_ADMIN_OR_STUDY_COORDINATOR)
  public ResponseEntity<Set<User>> searchUsers(
      @AuthenticationPrincipal @NotNull Jwt principal,
      @RequestParam(required = false)
          @ApiParam(
              value =
                  "A flag for controlling whether to list approved or not approved users, omitting it returns both)")
          Boolean approved,
      @RequestParam(required = false)
          @ApiParam(value = "A string contained in username, first or last name, or email")
          String search,
      @RequestParam(required = false)
          @ApiParam(
              value =
                  "A flag for controlling whether to include user's roles in the response (a bit slower)")
          Boolean withRoles) {
    return ResponseEntity.ok(
        userService.searchUsers(
            principal.getSubject(), approved, search, withRoles, Roles.extractRoles(principal)));
  }
}
