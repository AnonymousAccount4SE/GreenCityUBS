package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeePositionsDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.UserVO;
import greencity.exceptions.http.RemoteServerUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class UserRemoteClientFallbackFactory implements FallbackFactory<UserRemoteClient> {
    @Override
    public UserRemoteClient create(Throwable throwable) {
        return new UserRemoteClient() {
            @Override
            public String findUuidByEmail(String email) {
                throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_USER_DATA, throwable);
            }

            @Override
            public Optional<UserVO> findNotDeactivatedByEmail(String email) {
                log.error(ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST + email, throwable);
                return Optional.empty();
            }

            @Override
            public Optional<UbsCustomersDto> findByUuid(String uuid) {
                log.error(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, throwable);
                return Optional.empty();
            }

            @Override
            public boolean checkIfUserExistsByUuid(String uuid) {
                throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_USER_DATA, throwable);
            }

            @Override
            public void markUserDeactivated(String uuid) {
                throw new RemoteServerUnavailableException(ErrorMessage.USER_HAS_NOT_BEEN_DEACTIVATED, throwable);
            }

            @Override
            public PasswordStatusDto getPasswordStatus() {
                throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_PASSWORD_STATUS, throwable);
            }

            @Override
            public void sendEmailNotification(NotificationDto notification, String email) {
                log.error(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT, throwable);
            }

            @Override
            public Set<String> getAllAuthorities(String email) {
                log.error(ErrorMessage.COULD_NOT_RETRIEVE_EMPLOYEE_AUTHORITY, throwable);
                return Collections.singleton(throwable.getMessage());
            }

            @Override
            public PositionAuthoritiesDto getPositionsAndRelatedAuthorities(String email) {
                log.error(ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST + email, throwable);
                throw new RemoteServerUnavailableException(ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST, throwable);
            }

            @Override
            public List<String> getEmployeeLoginPositionNames(String email) {
                log.error(ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST + email);
                throw new RemoteServerUnavailableException(ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST, throwable);
            }

            @Override
            public void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto) {
                log.error(ErrorMessage.EMPLOYEE_AUTHORITY_WAS_NOT_EDITED, throwable);
                throw new RemoteServerUnavailableException(ErrorMessage.EMPLOYEE_AUTHORITY_WAS_NOT_EDITED, throwable);
            }

            @Override
            public void signUpEmployee(EmployeeSignUpDto dto) {
                log.error(ErrorMessage.EMPLOYEE_WAS_NOT_SUCCESSFULLY_SAVED, throwable);
                throw new RemoteServerUnavailableException(ErrorMessage.EMPLOYEE_WAS_NOT_SUCCESSFULLY_SAVED, throwable);
            }

            @Override
            public void updateEmployeeEmail(String newEmployeeEmail, String uuid) {
                log.error(ErrorMessage.EMPLOYEE_EMAIL_WAS_NOT_EDITED);
                throw new RemoteServerUnavailableException(ErrorMessage.EMPLOYEE_EMAIL_WAS_NOT_EDITED, throwable);
            }

            @Override
            public void updateAuthoritiesToRelatedPositions(EmployeePositionsDto dto) {
                log.error(ErrorMessage.EMPLOYEE_WAS_NOT_UPDATED);
                throw new RemoteServerUnavailableException(ErrorMessage.EMPLOYEE_WAS_NOT_UPDATED, throwable);
            }

            @Override
            public void deactivateEmployee(String uuid) {
                log.error(ErrorMessage.EMPLOYEE_WITH_CURRENT_UUID_WAS_NOT_DEACTIVATED);
                throw new RemoteServerUnavailableException(ErrorMessage.EMPLOYEE_WITH_CURRENT_UUID_WAS_NOT_DEACTIVATED,
                    throwable);
            }
        };
    }
}
