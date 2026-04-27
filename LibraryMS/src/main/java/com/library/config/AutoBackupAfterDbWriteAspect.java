package com.library.config;

import com.library.service.AdminMaintenanceService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Order(1000)
public class AutoBackupAfterDbWriteAspect {

    private static final ThreadLocal<Boolean> TX_SYNC_REGISTERED =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final AdminMaintenanceService adminMaintenanceService;

    public AutoBackupAfterDbWriteAspect(AdminMaintenanceService adminMaintenanceService) {
        this.adminMaintenanceService = adminMaintenanceService;
    }

    @Around("execution(* com.library.service..*(..)) && @annotation(transactional)")
    public Object backupAfterWriteTx(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        Object result = pjp.proceed();

        if (transactional.readOnly()) {
            return result;
        }
        if ("com.library.service.AdminMaintenanceService".equals(
                pjp.getSignature().getDeclaringTypeName())) {
            return result;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            if (!TX_SYNC_REGISTERED.get()) {
                TX_SYNC_REGISTERED.set(Boolean.TRUE);
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        adminMaintenanceService.backupAndCleanupSilently();
                    }

                    @Override
                    public void afterCompletion(int status) {
                        TX_SYNC_REGISTERED.remove();
                    }
                });
            }
        } else {
            adminMaintenanceService.backupAndCleanupSilently();
        }

        return result;
    }
}
