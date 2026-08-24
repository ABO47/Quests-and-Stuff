package com.abo47.questsandstuff.compat.oresandstuff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OresAndStuffCompatTest {
    @Test
    void initStaysInertWithoutOresAndStuffInstalled() {
        assertDoesNotThrow(OresAndStuffCompat::init);
    }

    @Test
    void grantTeamScanFailsClosedWithoutOresAndStuffInstalled() {
        boolean granted = OresAndStuffCompat.grantTeamScan(null, java.util.UUID.randomUUID(), null);

        assertFalse(granted);
    }
}
