package red.sigil.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class ChallengeMessage {

  private static final Random r = new Random();

  public final String targetName;
  public final byte[] targetInfo;
  public final int flags;

  public ChallengeMessage(String targetName, byte[] targetInfo, int flags) {
    this.targetName = targetName;
    this.targetInfo = targetInfo;
    this.flags = flags;
  }

  public byte[] toBytes() throws IOException {
    ByteBuffer dos = ByteBuffer.allocate(2048).order(ByteOrder.LITTLE_ENDIAN);

    byte[] targetName = (this.targetName != null ? this.targetName : "").getBytes(NTLMFlags.encoding(flags));
    byte[] targetInfo = this.targetInfo != null ? this.targetInfo : new byte[0];

    ByteArrayOutputStream payload = new ByteArrayOutputStream();

    // signature
    dos.put(NTLMFlags.SIGNATURE);
    // MessageType
    dos.putInt(2);

    // TargetNameFields
    dos.putShort((short) targetName.length);
    dos.putShort((short) targetName.length);
    dos.putInt(56 + payload.size());
    payload.write(targetName);

    // NegotiateFlags
    dos.putInt(flags);

    // ServerChallenge
    byte[] nonce = new byte[8];
    r.nextBytes(nonce);
    dos.put(nonce);

    // Reserved
    byte[] reserved = new byte[8];
    dos.put(reserved);

    // TargetInfoFields
    dos.putShort((short) targetInfo.length);
    dos.putShort((short) targetInfo.length);
    dos.putInt(56 + payload.size());
    payload.write(targetInfo);

    // Version
    byte[] version = new byte[8];
    dos.put(version);

    // Payload
    dos.put(payload.toByteArray());

    dos.flip();
    byte[] body = new byte[dos.limit()];
    dos.get(body);
    return body;
  }
}
