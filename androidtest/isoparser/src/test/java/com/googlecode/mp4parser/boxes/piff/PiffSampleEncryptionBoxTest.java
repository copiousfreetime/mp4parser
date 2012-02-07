package com.googlecode.mp4parser.boxes.piff;

import com.googlecode.mp4parser.boxes.AbstractSampleEncryptionBoxTest;
import org.junit.Before;


public class PiffSampleEncryptionBoxTest extends AbstractSampleEncryptionBoxTest {


    @Before
    public void setUp() throws Exception {
        senc = new PiffSampleEncryptionBox();
    }

}
