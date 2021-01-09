package net.sytes.jaraya.service;

import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Report;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.ReportRepo;
import net.sytes.jaraya.state.State;

public class ReportService {
    private ReportRepo reportRepo;
    private UserService userService;

    public ReportService(UserService userService) throws TelegramException {
        reportRepo = new ReportRepo();
        this.userService = userService;
    }

    public void report(Long userId) throws TelegramException {
        reportRepo.save(Report.builder()
                .user(userId)
                .build());
        if (reportRepo.getByIdUser(userId).size() > 5) {
            User user = userService.getByIdUser(userId);
            if (user != null) {
                user.setState(State.BANNED.name());
                userService.save(user);
            }
        }
    }

}