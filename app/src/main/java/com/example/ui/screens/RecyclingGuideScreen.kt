package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.WasteCategory
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

data class GuideDetail(
    val category: WasteCategory,
    val examplesEn: String,
    val examplesBm: String,
    val canRecycleEn: String,
    val canRecycleBm: String,
    val binColorEn: String,
    val binColorBm: String,
    val prepStepsEn: List<String>,
    val prepStepsBm: List<String>,
    val dosEn: List<String>,
    val dosBm: List<String>,
    val dontsEn: List<String>,
    val dontsBm: List<String>
)

@Composable
fun RecyclingGuideScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM

    val guides = remember {
        listOf(
            GuideDetail(
                category = WasteCategory.PLASTIC,
                examplesEn = "Water bottles (PET #1), Milk jugs (HDPE #2), Detergent containers, Bottle caps",
                examplesBm = "Botol air mineral (PET #1), Botol susu (HDPE #2), Bekas sabun pencuci, Penutup botol",
                canRecycleEn = "Yes (Clean rigid plastics)",
                canRecycleBm = "Ya (Plastik keras & bersih)",
                binColorEn = "Blue Recycling Bin (Plastik / Plastic)",
                binColorBm = "Tong Kitar Semula Biru (Plastik)",
                prepStepsEn = listOf("Rinse out liquids", "Crush to minimize volume", "Screw cap back on"),
                prepStepsBm = listOf("Bilas sisa cecair", "Kemekkan untuk jimat ruang", "Pasangkan semula penutup"),
                dosEn = listOf("Recycle clean bottles", "Separate straws if not recyclable"),
                dosBm = listOf("Kitar botol bersih", "Asingkan penyedut minuman jika kotor"),
                dontsEn = listOf("Do not recycle dirty plastic food wrappers", "No styrofoam in blue bin"),
                dontsBm = listOf("Jangan kitar plastik pembungkus berminyak", "Jangan campur polistirena")
            ),
            GuideDetail(
                category = WasteCategory.PAPER,
                examplesEn = "Cardboard boxes, notebooks, newspapers, office papers, magazines",
                examplesBm = "Kotak kadbod, buku nota, surat khabar, kertas pejabat, majalah",
                canRecycleEn = "Yes (Dry & grease-free)",
                canRecycleBm = "Ya (Kering & bebas minyak)",
                binColorEn = "Orange / Blue Paper Bin",
                binColorBm = "Tong Kertas (Jingga / Biru)",
                prepStepsEn = listOf("Flatten boxes", "Remove plastic tape and wire spirals", "Keep dry"),
                prepStepsBm = listOf("Ratakan kotak", "Buang pita pelekat & dawai kokot", "Pastikan sentiasa kering"),
                dosEn = listOf("Recycle shredded clean paper", "Bundle newspapers with twine"),
                dosBm = listOf("Kitar kertas pejabat bercetak", "Ikat surat khabar dengan tali"),
                dontsEn = listOf("Never recycle greasy pizza boxes", "No wet tissue paper or receipts"),
                dontsBm = listOf("Jangan masukkan kotak piza berminyak", "Jangan kitar tisu basah atau resit")
            ),
            GuideDetail(
                category = WasteCategory.GLASS,
                examplesEn = "Glass beverage bottles, sauce bottles, jam jars, perfume bottles",
                examplesBm = "Botol minuman kaca, botol kicap/sos, balang jem, botol minyak wangi",
                canRecycleEn = "Yes (100% infinitely recyclable)",
                canRecycleBm = "Ya (Boleh dikitar semula tanpa henti)",
                binColorEn = "Brown Glass Bin (Kaca)",
                binColorBm = "Tong Kaca Coklat",
                prepStepsEn = listOf("Rinse inside thoroughly", "Remove metal/plastic caps", "Handle gently"),
                prepStepsBm = listOf("Bilas bersih bahagian dalam", "Tanggalkan penutup logam/plastik", "Kendalikan cermat"),
                dosEn = listOf("Recycle clear, green, and amber glass jars"),
                dosBm = listOf("Kitar kaca jernih, hijau dan coklat"),
                dontsEn = listOf("No mirrors, lightbulbs, or window panes in glass bin"),
                dontsBm = listOf("Jangan campur cermin muka, mentol atau kaca tingkap")
            ),
            GuideDetail(
                category = WasteCategory.METAL,
                examplesEn = "Aluminum soda cans, canned food tins, steel tins, aluminum foil",
                examplesBm = "Tin minuman aluminium, tin makanan, tin keluli, kerajang aluminium",
                canRecycleEn = "Yes (High recycling value)",
                canRecycleBm = "Ya (Nilai kitar semula yang tinggi)",
                binColorEn = "Orange Metal Bin (Logam)",
                binColorBm = "Tong Logam Jingga",
                prepStepsEn = listOf("Rinse food sauce residues", "Flatten or squash tin cans"),
                prepStepsBm = listOf("Bilas sisa kuah/makanan", "Kemekkan tin"),
                dosEn = listOf("Clean aluminum foil balls", "Recycle empty soup cans"),
                dosBm = listOf("Gumpalkan kerajang aluminium bersih", "Kitar tin sardin yang dibasuh"),
                dontsEn = listOf("No aerosol cans with remaining pressure", "No toxic chemical cans"),
                dontsBm = listOf("Jangan buang tin aerosol bertekanan", "Jangan kitar tin racun kimia")
            ),
            GuideDetail(
                category = WasteCategory.E_WASTE,
                examplesEn = "Smartphone batteries, old chargers, keyboards, dead laptops, circuit boards",
                examplesBm = "Bateri telefon, pengecas lama, papan kekunci, komputer riba rosak, papan litar",
                canRecycleEn = "Special Collection Required ⚠️",
                canRecycleBm = "Pengumpulan Khas Diperlukan ⚠️",
                binColorEn = "Red / Purple E-Waste Kiosk",
                binColorBm = "Kiosk Khas E-Sisa Sekolah & Komuniti",
                prepStepsEn = listOf("Tape battery terminals", "Do not puncture or crush batteries"),
                prepStepsBm = listOf("Tampal pita pelekat pada kutub bateri", "Jangan tebuk atau bakar bateri"),
                dosEn = listOf("Drop at authorized school e-waste kiosk"),
                dosBm = listOf("Hantar ke kiosk e-sisa sekolah yang berdaftar"),
                dontsEn = listOf("Never toss into regular garbage dumpsters!"),
                dontsBm = listOf("JANGAN sekali-kali buang ke dalam tong sampah rumah!")
            ),
            GuideDetail(
                category = WasteCategory.ORGANIC,
                examplesEn = "Fruit peels, vegetable trimmings, tea bags, garden leaves",
                examplesBm = "Kulit buah-buahan, sisa sayur, uncang teh, daun kering sekolah",
                canRecycleEn = "Compostable 🌱",
                canRecycleBm = "Boleh Dikompos 🌱",
                binColorEn = "Green Compost Bin",
                binColorBm = "Tong Kompos Sekolah / Baja Hijau",
                prepStepsEn = listOf("Chop into small pieces", "Mix greens with dry brown leaves"),
                prepStepsBm = listOf("Potong kecil sisa makanan", "Campur bersama daun kering"),
                dosEn = listOf("Use for school garden organic composting"),
                dosBm = listOf("Gunakan untuk projek baja kompos taman sekolah"),
                dontsEn = listOf("No plastic tags or fruit stickers in compost"),
                dontsBm = listOf("Pastikan tiada pelekat buah atau tali plastik")
            ),
            GuideDetail(
                category = WasteCategory.HAZARDOUS,
                examplesEn = "Insect spray cans, paint thinners, fluorescent tubes, acid bottles",
                examplesBm = "Tin racun serangga, pencair cat, lampu kalimantang, botol asid",
                canRecycleEn = "Hazardous - Licensed Facility Only ⚠️",
                canRecycleBm = "Berbahaya - Fasiliti Berlesen Sahaja ⚠️",
                binColorEn = "Designated Hazardous Drop-off",
                binColorBm = "Pusat Pelupusan Sisa Terjadual",
                prepStepsEn = listOf("Keep in original container", "Store out of reach of children"),
                prepStepsBm = listOf("Simpan dalam bekas asal", "Jauhkan daripada kanak-kanak"),
                dosEn = listOf("Consult municipal hazardous collection schedule"),
                dosBm = listOf("Semak jadual kutipan sisa berbahaya pihak berkuasa tempatan"),
                dontsEn = listOf("Never pour down sewer drains or sinks!"),
                dontsBm = listOf("JANGAN sesekali tuang ke dalam longkang atau sinki!")
            )
        )
    }

    var expandedCat by remember { mutableStateOf<WasteCategory?>(WasteCategory.PLASTIC) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (isBm) "Panduan Kitar Semula 3R Lengkap" else "Complete 3R Recycling Guide",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isBm)
                        "Ketahui cara penyediaan, pengasingan dan etika kitar semula bagi setiap kategori sisa."
                    else
                        "Learn preparation steps, separation dos and don'ts, and bin colors for every material.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        items(guides) { guide ->
            val isExpanded = expandedCat == guide.category
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedCat = if (isExpanded) null else guide.category
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(guide.category.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(guide.category.iconEmoji, fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = guide.category.getDisplayName(language),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isBm) guide.canRecycleBm else guide.canRecycleEn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = guide.category.color,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))

                            // Bin Color info
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = guide.category.color.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🗑️", fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${if (isBm) "Tong Disyorkan:" else "Target Bin:"} ${if (isBm) guide.binColorBm else guide.binColorEn}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = guide.category.color
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Examples
                            Text(
                                text = if (isBm) "Contoh Barangan:" else "Common Examples:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isBm) guide.examplesBm else guide.examplesEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )

                            Spacer(Modifier.height(10.dp))

                            // Prep steps
                            Text(
                                text = if (isBm) "Langkah Penyediaan:" else "Preparation Steps:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            val prep = if (isBm) guide.prepStepsBm else guide.prepStepsEn
                            prep.forEachIndexed { i, step ->
                                Text(
                                    text = "• $step",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Dos and Don'ts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Dos
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EcoGreenPrimary.copy(alpha = 0.08f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = "Do", tint = EcoGreenPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isBm) "Perlu" else "Do", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EcoGreenPrimary)
                                        }
                                        val dos = if (isBm) guide.dosBm else guide.dosEn
                                        dos.forEach { d ->
                                            Text(text = "• $d", fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                                        }
                                    }
                                }

                                // Don'ts
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Close, contentDescription = "Don't", tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isBm) "Elak" else "Don't", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFDC2626))
                                        }
                                        val donts = if (isBm) guide.dontsBm else guide.dontsEn
                                        donts.forEach { dn ->
                                            Text(text = "• $dn", fontSize = 10.sp, color = Color(0xFF991B1B), modifier = Modifier.padding(top = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
