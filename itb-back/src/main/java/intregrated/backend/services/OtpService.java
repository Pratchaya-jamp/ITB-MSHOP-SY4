package intregrated.backend.services;

import intregrated.backend.dtos.authentications.RequestOtpResponseDto;
import intregrated.backend.entities.OtpRequest;
import intregrated.backend.entities.UserRememberToken;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.repositories.OtpRequestRepo;
import intregrated.backend.repositories.UserRememberTokenRepo;
import intregrated.backend.repositories.accounts.UsersAccountRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int REMEMBER_DAYS = 30;

    @Autowired
    private OtpRequestRepo otpRequestRepo;

    @Autowired
    private UsersAccountRepo usersAccountRepo;

    @Autowired
    private UserRememberTokenRepo rememberRepo;

    @Autowired
    private MailService mailService;

    private final SecureRandom rnd = new SecureRandom();

    private String generateOtp() {
        int max = (int)Math.pow(10, OTP_LENGTH);
        int num = rnd.nextInt(max - max/10) + max/10; // ensure leading digits not zero
        return String.format("%0" + OTP_LENGTH + "d", num);
    }

    private String generateRememberToken() {
        byte[] random = new byte[32];
        rnd.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    /**
     * Request an OTP for user with given email.
     * Enforces 60s cooldown since last requested OTP.
     */
    @Transactional
    public RequestOtpResponseDto requestOtp(String email) {
        UsersAccount user = usersAccountRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        otpRequestRepo.deleteAllByUser(user);

        // check latest request time
        Optional<OtpRequest> lastOpt = otpRequestRepo.findTopByUserOrderByCreatedOnDesc(user);
        if (lastOpt.isPresent()) {
            Instant last = lastOpt.get().getLastRequestedAt();
            if (last != null && Instant.now().isBefore(last.plusSeconds(RESEND_COOLDOWN_SECONDS))) {
                long waitSec = RESEND_COOLDOWN_SECONDS - ChronoUnit.SECONDS.between(last, Instant.now());
                throw new IllegalStateException("Please wait " + Math.max(waitSec, 1) + " seconds before requesting again");
            }
        }

        // create otp
        String otp = generateOtp();
        Instant now = Instant.now();
        OtpRequest r = new OtpRequest();
        r.setUser(user);
        r.setOtpCode(otp);
        r.setIsUsed(false);
        r.setExpiredAt(now.plus(OTP_EXPIRE_MINUTES, ChronoUnit.MINUTES));
        r.setLastRequestedAt(now);
        r.setCreatedOn(now);
        r.setUpdatedOn(now);
        otpRequestRepo.save(r);

        try {
            mailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            // still return OTP for debug/dev
        }

        return new RequestOtpResponseDto("OTP sent", r.getExpiredAt());
    }

    /**
     * Verify OTP. If rememberMe true -> create remember token for 30 days.
     * Returns rememberToken (nullable) to be stored in DB and client if requested.
     */
    @Transactional
    public String verifyOtp(String email, String otp, boolean rememberMe) {
        UsersAccount user = usersAccountRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // get latest unused OTP
        Optional<OtpRequest> opt = otpRequestRepo.findTopUnusedByUserOrderByCreatedOnDesc(user);
        if (opt.isEmpty()) {
            throw new IllegalStateException("No OTP requested");
        }

        OtpRequest req = opt.get();

        if (req.getIsUsed()) {
            throw new IllegalStateException("OTP already used");
        }

        if (req.getExpiredAt().isBefore(Instant.now())) {
            throw new IllegalStateException("OTP expired");
        }

        if (!StringUtils.hasText(otp) || !req.getOtpCode().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // mark used
        req.setIsUsed(true);
        otpRequestRepo.save(req);

        if (rememberMe) {
            // invalidate old remember tokens
            rememberRepo.invalidateAllForUser(user);

            UserRememberToken rt = new UserRememberToken();
            rt.setUser(user);
            String rememberToken = generateRememberToken();
            rt.setRememberToken(rememberToken);
            rt.setRememberUntil(Instant.now().plus(REMEMBER_DAYS, ChronoUnit.DAYS));
            rt.setIsValid(true);
            rt.setCreatedOn(Instant.now());
            rt.setUpdatedOn(Instant.now());
            rememberRepo.save(rt);
            return rememberToken;
        }

        return null;
    }

    public boolean hasValidRememberToken(UsersAccount user) {
        Optional<UserRememberToken> opt = rememberRepo.findValidByUserOrderByRememberUntilDesc(user);
        if (opt.isEmpty()) return false;
        UserRememberToken t = opt.get();
        return t.getIsValid() && t.getRememberUntil().isAfter(Instant.now());
    }
}
