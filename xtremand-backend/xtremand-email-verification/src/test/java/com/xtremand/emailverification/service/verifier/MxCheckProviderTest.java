package com.xtremand.emailverification.service.verifier;

import com.xtremand.emailverification.service.factory.LookupFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xbill.DNS.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MxCheckProviderTest {

    @Mock
    private LookupFactory lookupFactory;

    @Mock
    private Lookup lookup;

    @InjectMocks
    private MxCheckProvider mxCheckProvider;

    @BeforeEach
    void setUp() throws TextParseException {
        // Default mock setup
        when(lookupFactory.create(anyString(), anyInt())).thenReturn(lookup);
    }

    @Test
    void testHasMxRecords_Success() {
        Record[] records = new Record[]{new MXRecord(Name.fromConstantString("example.com."), DClass.IN, 3600, 10, Name.fromConstantString("mx.example.com."))};
        when(lookup.run()).thenReturn(records);
        when(lookup.getResult()).thenReturn(Lookup.SUCCESSFUL);

        assertTrue(mxCheckProvider.hasMxRecords("example.com"));
    }

    @Test
    void testHasMxRecords_NoRecordsFound() {
        when(lookup.run()).thenReturn(null);
        when(lookup.getResult()).thenReturn(Lookup.SUCCESSFUL); // Successful lookup, but no records

        assertFalse(mxCheckProvider.hasMxRecords("norecords.com"));
    }

    @Test
    void testHasMxRecords_LookupFailed() {
        when(lookup.run()).thenReturn(null);
        when(lookup.getResult()).thenReturn(Lookup.HOST_NOT_FOUND);
        when(lookup.getErrorString()).thenReturn("Host not found");

        assertFalse(mxCheckProvider.hasMxRecords("nonexistent.com"));
    }

    @Test
    void testGetMxHosts_Success() throws TextParseException {
        Record[] records = new Record[]{
                new MXRecord(Name.fromConstantString("example.com."), DClass.IN, 3600, 20, Name.fromConstantString("mx2.example.com.")),
                new MXRecord(Name.fromConstantString("example.com."), DClass.IN, 3600, 10, Name.fromConstantString("mx1.example.com."))
        };
        when(lookup.run()).thenReturn(records);
        when(lookup.getResult()).thenReturn(Lookup.SUCCESSFUL);

        List<String> mxHosts = mxCheckProvider.getMxHosts("example.com");

        assertNotNull(mxHosts);
        assertEquals(2, mxHosts.size());
        assertEquals("mx1.example.com.", mxHosts.get(0)); // Check for correct sorting by priority
        assertEquals("mx2.example.com.", mxHosts.get(1));
    }

    @Test
    void testGetMxHosts_Failure() {
        when(lookup.run()).thenReturn(null);
        when(lookup.getResult()).thenReturn(Lookup.TYPE_NOT_FOUND);

        List<String> mxHosts = mxCheckProvider.getMxHosts("no-mx.com");

        assertNotNull(mxHosts);
        assertTrue(mxHosts.isEmpty());
    }
}