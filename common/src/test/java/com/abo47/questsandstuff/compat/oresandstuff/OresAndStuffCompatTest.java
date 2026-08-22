package com.abo47.questsandstuff.compat.oresandstuff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OresAndStuffCompatTest {
    @Test
    void initStaysInertWithoutOresAndStuffInstalled() {
        assertDoesNotThrow(OresAndStuffCompat::init);
    }

    @Test
    void grantTeamScanFailsClosedWithoutOresAndStuffInstalled() {
        boolean granted = OresAndStuffCompat.grantTeamScan(null, java.util.UUID.randomUUID(), null);

        org.junit.jupiter.api.Assertions.assertFalse(granted);
    }
}
