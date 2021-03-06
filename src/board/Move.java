package board;

/**
 * TODO
 */
public class Move implements Comparable<Move> {
    // 0-1: Special (1 = promotion, 2 = en passant, 3 = castling)
    // 2-7: Destination square
    // 8-13: Origin square
    // 14-15: Promotion piece

    // // @ formatter:off
    // private static final int TYPE_MASK      = 0b0000000000000011;
    // private static final int DEST_MASK      = 0b0000000011111100;
    // private static final int SRC_MASK       = 0b0011111100000000;
    // private static final int PROMOTION_MASK = 0b1100000000000000;
    //
    // private static final int DEST_SHIFT = 2;
    // private static final int SRC_SHIFT = 8;
    // private static final int PROMOTION_SHIFT = 14;
    // // @ formatter:on
    //
    // public static char instance(byte type, byte src, byte dest, byte promotionPiece) {
    //     char move = 0;
    //
    //     move |= type;
    //     move |= dest << DEST_SHIFT;
    //     move |= src << SRC_SHIFT;
    //     move |= promotionPiece << PROMOTION_SHIFT;
    //
    //     return move;
    // }

    public static final byte KINGSIDE_CASTLE = 1;
    public static final byte QUEENSIDE_CASTLE = 2;

    public static final byte PROMOTION_MASK = 0b1000;
    public static final byte CAPTURE_MASK = 0b0100;
    public static final byte SPECIAL_MASK = 0b0011;

    public final Piece src;
    public final Piece dest;
    public final int score;
    // Bit 0: Special 0 -- Used to store additional data
    // Bit 1: Special 1 -- Used to store additional data
    // Bit 2: Capture   -- 1 iff this move is a capture
    // Bit 3: Promotion -- 1 iff this move is a promotion
    public final byte code;

    public Move(Piece src, Piece dest, int score, byte castle) {
        this(src, dest, (byte) 0, score, false, castle);
    }

    public Move(Piece src, Piece dest, byte promotion, int score, boolean isCapture) {
        this(src, dest, promotion, score, isCapture, (byte) 0);
    }

    private Move(Piece src, Piece dest, byte promotion, int score, boolean isCapture, byte castle) {
        this.src = src;
        this.dest = dest;
        this.score = score;

        byte b = 0;
        if (isCapture) {
            b |= CAPTURE_MASK;
        }
        if (castle == KINGSIDE_CASTLE) {
            // King-side castle
            b |= 0b0010;
        }
        if (castle == QUEENSIDE_CASTLE) {
            // Queen-side castle
            b |= 0b0011;
        }
        if (promotion != 0) {
            // Set what piece this is promoted to
            b |= (promotion - 1) & SPECIAL_MASK;
            b |= PROMOTION_MASK;
        }
        // Set if this is a double pawn push
        if (src.type() == Piece.Type.PAWN) {
            int srcRank = src.rank();
            int destRank = dest.rank();
            if (srcRank + 2 == destRank || srcRank - 2 == destRank) {
                b = 0b0001;
            }
        }
        // Set if this is an en passant capture
        if (src.type() == Piece.Type.PAWN && dest.type() == Piece.Type.EMPTY && src.file() != dest.file()) {
            b = 0b0101;
        }

        this.code = b;
    }

    /**
     * Returns whether this move is a pawn double push.
     *
     * @return True iff this move is a pawn double push.
     */
    public boolean isDoublePush() {
        return this.code == 0b0001;
    }

    /**
     * Returns whether this move is an en passant capture.
     *
     * @return True iff this move is an en passant capture.
     */
    public boolean isEnpassant() {
        return this.code == 0b0101;
    }

    /**
     * Returns whether this move is a capture.
     *
     * @return True iff this move is a capture.
     */
    public boolean isCapture() {
        return (this.code & CAPTURE_MASK) != 0;
    }

    /**
     * Returns whether this move is a castling move.
     *
     * @return True iff this move is a castling move.
     */
    public boolean isCastle() {
        byte special = (byte) (this.code & SPECIAL_MASK);
        return !isCapture() && !isPromotion() && (special == 0b10 || special == 0b11);
    }

    /**
     * Returns what kind of castling move this is.
     *
     * @return 0 if this isn't a castling move, 1 if it is a king side castle, 2
     * if it is a queen side castle.
     */
    public byte castleType() {
        if (!isCastle()) {
            return 0;
        }
        return (byte) ((this.code == 0b0010) ? 1 : 2);
    }

    /**
     * Returns whether this move is a promotion.
     *
     * @return True iff this move is a promotion.
     */
    public boolean isPromotion() {
        return (this.code & PROMOTION_MASK) != 0;
    }

    /**
     * Returns the piece type of this move's promotion.
     *
     * @return The piece type of this move's promotion, or 0 if this isn't a
     * promotion move.
     */
    public byte promotionPiece() {
        if (!isPromotion()) {
            return 0;
        }
        return (byte) ((this.code & SPECIAL_MASK) + 1);
    }

    @Override
    public int compareTo(Move o) {
        return o.score - this.score;
    }

    @Override
    public String toString() {
        String promotion = "";
        if (isPromotion()) {
            promotion = String.valueOf(Bitboard.PIECES.charAt(promotionPiece()));
        }
        return this.src.toString() + this.dest.toString() + promotion;
        // char srcFile = (char) ('a' + this.src.file());
        // int srcRank = 1 + this.src.rank();
        // char destFile = (char) ('a' + this.dest.file());
        // int destRank = 1 + this.dest.rank();
        //
        // return "" + srcFile + srcRank + destFile + destRank;
    }
}
