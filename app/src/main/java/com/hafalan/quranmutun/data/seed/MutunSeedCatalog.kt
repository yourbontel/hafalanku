package com.hafalan.quranmutun.data.seed

import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.MutunSectionEntity

data class MutunSeedTemplate(
    val item: MutunItemEntity,
    val sections: List<Pair<String, String>> // (title, unitRangeLabel)
)

object MutunSeedCatalog {
    val defaults: List<MutunSeedTemplate> = listOf(
        MutunSeedTemplate(
            item = MutunItemEntity(
                title = "Tuhfatul Athfal",
                author = "Syaikh Sulaiman Al-Jamzuri",
                category = "Tajwid",
                unitLabel = "Bait",
                totalUnits = 61
            ),
            sections = listOf(
                "Muqaddimah" to "Bait 1–5",
                "Ahkamun Nun As-Sakinah wat Tanwin" to "Bait 6–16",
                "Ahkamun Nun wal Mim Al-Musyaddadatain" to "Bait 17",
                "Ahkamul Mim As-Sakinah" to "Bait 18–23",
                "Hukmu Lam Al wa Lam Fi'il" to "Bait 24–29",
                "Fil Mitslain wal Mutaqaribain wal Mutajanisain" to "Bait 30–34",
                "Aqsamul Madd" to "Bait 35–41",
                "Ahkamul Madd" to "Bait 42–47",
                "Aqsamul Madd Al-Lazim & Khatimah" to "Bait 48–61"
            )
        ),
        MutunSeedTemplate(
            item = MutunItemEntity(
                title = "Muqaddimah Al-Jazariyah",
                author = "Imam Ibnul Jazari",
                category = "Tajwid",
                unitLabel = "Bait",
                totalUnits = 109
            ),
            sections = listOf(
                "Muqaddimah" to "Bait 1–8",
                "Makharijul Huruf" to "Bait 9–19",
                "Shifatul Huruf" to "Bait 20–26",
                "Babut Tajwid" to "Bait 27–33",
                "Babut Tafkhim wat Tarqiq" to "Bait 34–43",
                "Babul Idgham & Dhad wa Zha" to "Bait 44–61",
                "Ahkamun Nun wal Mim & Al-Madd" to "Bait 62–72",
                "Babul Waqf wal Ibtida'" to "Bait 73–78",
                "Al-Maqthu' wal Maushul & Ta'at" to "Bait 79–100",
                "Hamzah Washal & Khatimah" to "Bait 101–109"
            )
        ),
        MutunSeedTemplate(
            item = MutunItemEntity(
                title = "Al-Arba'in An-Nawawiyah",
                author = "Imam An-Nawawi",
                category = "Hadits",
                unitLabel = "Hadits",
                totalUnits = 42
            ),
            sections = listOf(
                "Hadits 1–10 (Niat, Islam-Iman-Ihsan, Halal-Haram)" to "Hadits 1–10",
                "Hadits 11–20 (Wara', Akhlak, Malu, Istiqamah)" to "Hadits 11–20",
                "Hadits 21–30 (Taqwa, Kebaikan, Larangan Zalim)" to "Hadits 21–30",
                "Hadits 31–42 (Zuhud, Ukhuwah, Pengampunan Allah)" to "Hadits 31–42"
            )
        ),
        MutunSeedTemplate(
            item = MutunItemEntity(
                title = "Al-Ushul Ats-Tsalatsah",
                author = "Syaikh Muhammad bin Abdul Wahhab",
                category = "Aqidah",
                unitLabel = "Bagian",
                totalUnits = 5
            ),
            sections = listOf(
                "Empat Perkara Wajib & Tiga Perkara Penting" to "Bagian 1",
                "Landasan Pertama: Mengenal Allah" to "Bagian 2",
                "Landasan Kedua: Mengenal Agama Islam" to "Bagian 3",
                "Landasan Ketiga: Mengenal Nabi Muhammad ﷺ" to "Bagian 4",
                "Penutup: Hari Kebangkitan & Thaghut" to "Bagian 5"
            )
        ),
        MutunSeedTemplate(
            item = MutunItemEntity(
                title = "Al-Qawa'id Al-Arba'",
                author = "Syaikh Muhammad bin Abdul Wahhab",
                category = "Aqidah",
                unitLabel = "Kaidah",
                totalUnits = 4
            ),
            sections = listOf(
                "Muqaddimah & Kaidah Pertama" to "Kaidah 1",
                "Kaidah Kedua" to "Kaidah 2",
                "Kaidah Ketiga" to "Kaidah 3",
                "Kaidah Keempat" to "Kaidah 4"
            )
        ),
        MutunSeedTemplate(
            item = MutunItemEntity(
                title = "Matan Al-Ajurrumiyah",
                author = "Imam Ibnu Ajurrum",
                category = "Nahwu",
                unitLabel = "Bab",
                totalUnits = 26
            ),
            sections = listOf(
                "Al-Kalam & Al-I'rab" to "Bab 1–3",
                "Al-Af'al & Marfu'atul Asma'" to "Bab 4–12",
                "Al-Awamil Ad-Dakhilah & Tawabi'" to "Bab 13–17",
                "Manshubatul Asma' & Makhfudhat" to "Bab 18–26"
            )
        )
    )

    fun buildSections(mutunId: Long, template: MutunSeedTemplate): List<MutunSectionEntity> =
        template.sections.mapIndexed { index, (title, range) ->
            MutunSectionEntity(
                mutunId = mutunId,
                orderIndex = index,
                title = title,
                unitRangeLabel = range
            )
        }
}
