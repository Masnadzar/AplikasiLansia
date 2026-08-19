package com.alya.aplikasilansia.data;

/**
 * Interpretasi hasil skrining GDS-15 (Geriatric Depression Scale - Short Form).
 * Skala 4 tingkat: 0-4 Normal, 5-8 Ringan, 9-11 Sedang, 12-15 Berat
 * (skala ini konsisten dengan QuizViewModel.classifyScore()).
 *
 * PENTING: ini adalah alat skrining awal, BUKAN alat diagnosis medis.
 * Redaksi teks sengaja dibuat suportif dan mendorong konsultasi profesional,
 * bukan vonis atau label yang menghakimi, mengingat target pengguna adalah lansia.
 */
public class GdsInterpretation {

    public static class Result {
        public final int level; // 0=Normal, 1=Ringan, 2=Sedang, 3=Berat
        public final String title;
        public final String kesimpulan;
        public final String saran;

        Result(int level, String title, String kesimpulan, String saran) {
            this.level = level;
            this.title = title;
            this.kesimpulan = kesimpulan;
            this.saran = saran;
        }
    }

    public static Result getInterpretation(int score) {
        if (score <= 4) {
            return new Result(
                    0,
                    "Normal",
                    "Berdasarkan jawaban Anda, tidak ditemukan indikasi gejala depresi yang signifikan pada skrining ini. Suasana hati dan semangat Anda tergolong baik dan stabil.",
                    "• Tetap jaga rutinitas harian yang menyenangkan\n" +
                            "• Pertahankan hubungan sosial dengan keluarga dan teman\n" +
                            "• Lakukan aktivitas fisik ringan secara teratur\n" +
                            "• Lakukan skrining ini secara berkala untuk memantau kondisi Anda"
            );
        } else if (score <= 8) {
            return new Result(
                    1,
                    "Depresi Ringan",
                    "Hasil skrining menunjukkan beberapa gejala yang mengarah pada depresi ringan. Ini adalah tanda awal yang baik untuk diperhatikan, meski belum tentu memerlukan penanganan intensif.",
                    "• Cobalah lebih sering berbincang dengan keluarga atau teman dekat\n" +
                            "• Luangkan waktu untuk kegiatan yang Anda sukai\n" +
                            "• Jaga pola tidur dan makan yang teratur\n" +
                            "• Jika gejala terus berlanjut, coba ceritakan pada tenaga kesehatan terdekat"
            );
        } else if (score <= 11) {
            return new Result(
                    2,
                    "Depresi Sedang",
                    "Hasil skrining menunjukkan gejala depresi pada tingkat sedang. Kondisi ini sebaiknya tidak diabaikan, karena bisa memengaruhi kualitas hidup sehari-hari Anda.",
                    "• Sebaiknya diskusikan hasil ini dengan petugas kesehatan di Puskesmas terdekat\n" +
                            "• Ajak keluarga atau pendamping untuk menemani Anda\n" +
                            "• Hindari menyendiri dalam waktu lama\n" +
                            "• Tetap lakukan aktivitas ringan yang membuat Anda nyaman"
            );
        } else {
            return new Result(
                    3,
                    "Depresi Berat",
                    "Hasil skrining menunjukkan gejala depresi pada tingkat berat. Penting untuk segera menindaklanjuti hasil ini bersama tenaga kesehatan profesional.",
                    "• Segera diskusikan hasil ini dengan dokter atau psikolog\n" +
                            "• Informasikan hasil ini kepada keluarga atau pendamping Anda\n" +
                            "• Jangan ragu meminta bantuan jika merasa kesulitan\n" +
                            "• Puskesmas atau layanan kesehatan terdekat siap membantu Anda"
            );
        }
    }
}