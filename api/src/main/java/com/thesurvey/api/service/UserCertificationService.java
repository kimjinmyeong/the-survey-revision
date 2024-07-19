package com.thesurvey.api.service;

import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.domain.UserCertification;
import com.thesurvey.api.dto.request.user.UserCertificationUpdateRequestDto;
import com.thesurvey.api.dto.response.userCertification.UserCertificationListDto;
import com.thesurvey.api.repository.UserCertificationRepository;
import com.thesurvey.api.service.mapper.UserCertificationMapper;
import com.thesurvey.api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCertificationService {

    private final UserCertificationRepository userCertificationRepository;
    private final UserCertificationMapper userCertificationMapper;

    @Transactional(readOnly = true)
    public UserCertificationListDto getUserCertifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);
        log.info("Fetching certifications for user ID: {}", userId);
        return userCertificationMapper.toUserCertificationListDto(userId);
    }

    @Transactional
    public UserCertificationListDto updateUserCertification(Authentication authentication,
                                                            UserCertificationUpdateRequestDto userCertificationUpdateRequestDto) {
        User user = UserUtil.getUserFromAuthentication(authentication);
        log.info("Updating certifications for user ID: {}", user.getUserId());
        List<Integer> userCertificationList =
                userCertificationRepository.findUserCertificationTypeByUserId(user.getUserId());

        if (userCertificationUpdateRequestDto.getIsKakaoCertificated()) {
            log.debug("Updating Kakao certification for user ID: {}", user.getUserId());
            saveUserCertification(user, CertificationType.KAKAO, userCertificationList);
        }

        if (userCertificationUpdateRequestDto.getIsNaverCertificated()) {
            log.debug("Updating Naver certification for user ID: {}", user.getUserId());
            saveUserCertification(user, CertificationType.NAVER, userCertificationList);
        }

        if (userCertificationUpdateRequestDto.getIsGoogleCertificated()) {
            log.debug("Updating Google certification for user ID: {}", user.getUserId());
            saveUserCertification(user, CertificationType.GOOGLE, userCertificationList);
        }

        if (userCertificationUpdateRequestDto.getIsWebMailCertificated()) {
            log.debug("Updating WebMail certification for user ID: {}", user.getUserId());
            saveUserCertification(user, CertificationType.WEBMAIL, userCertificationList);
        }

        if (userCertificationUpdateRequestDto.getIsDriverLicenseCertificated()) {
            log.debug("Updating Driver License certification for user ID: {}", user.getUserId());
            saveUserCertification(user, CertificationType.DRIVER_LICENSE, userCertificationList);
        }

        if (userCertificationUpdateRequestDto.getIsIdentityCardCertificated()) {
            log.debug("Updating Identity Card certification for user ID: {}", user.getUserId());
            saveUserCertification(user, CertificationType.IDENTITY_CARD, userCertificationList);
        }

        log.info("Certifications updated for user ID: {}", user.getUserId());
        return userCertificationMapper.toUserCertificationListDto(user.getUserId());
    }

    /**
     * This method is a scheduled task that runs every day at midnight
     * (in the "Asia/Seoul" timezone) to delete expired user certifications from the database.
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void deleteExpiredCertificates() {
        log.info("Deleting expired certifications");
        userCertificationRepository.deleteByExpirationDateLessThanEqual(
                LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        log.info("Expired certifications deleted");
    }

    private void saveUserCertification(User user, CertificationType certificationType,
                                       List<Integer> userCertificationList) {
        if (!userCertificationList.contains(certificationType.getCertificationTypeId())) {
            log.info("Saving new certification of type: {} for user ID: {}", certificationType, user.getUserId());
            userCertificationRepository.save(buildUserCertification(user, certificationType));
            log.info("Certification of type: {} saved for user ID: {}", certificationType, user.getUserId());
        }
    }

    private UserCertification buildUserCertification(User user, CertificationType certificationType) {
        return UserCertification.builder()
                .user(user)
                .certificationType(certificationType)
                .certificationDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                // The expiration date is set to 2 years after the current date.
                .expirationDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusYears(2))
                .build();
    }
}
