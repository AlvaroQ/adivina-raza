package com.alvaroquintana.adivinaperro.managers

import com.alvaroquintana.domain.BreedPrediction
import com.alvaroquintana.domain.Dog
import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.data.db.AdivinaRazaDatabase
import java.text.Normalizer

class AndroidLabelBreedMapper(
    private val database: AdivinaRazaDatabase,
    private val dataBaseSource: DataBaseSource
) : LabelBreedMapper {

    private val labelToDbName: Map<String, String> = mapOf(
        "n02085620-Chihuahua" to "Chihuahua",
        "n02085782-Japanese_spaniel" to "Spaniel japonés",
        "n02085936-Maltese_dog" to "Bichón maltés",
        "n02086079-Pekinese" to "Pekinés",
        "n02086240-Shih-Tzu" to "Shih Tzu",
        "n02086646-Blenheim_spaniel" to "Cavalier King Charles Spaniel",
        "n02086910-papillon" to "Papillón",
        "n02087046-toy_terrier" to "Terrier inglés miniatura",
        "n02087394-Rhodesian_ridgeback" to "Crestado rodesiano",
        "n02088094-Afghan_hound" to "Galgo afgano",
        "n02088238-basset" to "Basset Hound",
        "n02088364-beagle" to "Beagle",
        "n02088466-bloodhound" to "Perro de San Huberto",
        "n02088632-bluetick" to "Black and Tan Coonhound",
        "n02089078-black-and-tan_coonhound" to "Black and Tan Coonhound",
        "n02089867-Walker_hound" to "Foxhound inglés",
        "n02089973-English_foxhound" to "Foxhound inglés",
        "n02090379-redbone" to "Black and Tan Coonhound",
        "n02090622-borzoi" to "Borzoi",
        "n02090721-Irish_wolfhound" to "Lebrel irlandés",
        "n02091032-Italian_greyhound" to "Galgo italiano",
        "n02091134-whippet" to "Whippet",
        "n02091244-Ibizan_hound" to "Podenco ibicenco",
        "n02091467-Norwegian_elkhound" to "Cazador de alces noruego",
        "n02091635-otterhound" to "Otterhound",
        "n02091831-Saluki" to "Saluki",
        "n02092002-Scottish_deerhound" to "Lebrel escocés",
        "n02092339-Weimaraner" to "Braco de Weimar",
        "n02093256-Staffordshire_bullterrier" to "Staffordshire bull terrier",
        "n02093428-American_Staffordshire_terrier" to "American Staffordshire Terrier",
        "n02093647-Bedlington_terrier" to "Bedlington Terrier",
        "n02093754-Border_terrier" to "Border Terrier",
        "n02093859-Kerry_blue_terrier" to "Kerry Blue Terrier",
        "n02093991-Irish_terrier" to "Terrier irlandés",
        "n02094114-Norfolk_terrier" to "Terrier de Norfolk y Norwich",
        "n02094258-Norwich_terrier" to "Terrier de Norfolk y Norwich",
        "n02094433-Yorkshire_terrier" to "Yorkshire Terrier",
        "n02095314-wire-haired_fox_terrier" to "Fox Terrier",
        "n02095570-Lakeland_terrier" to "Lakeland Terrier",
        "n02095889-Sealyham_terrier" to "Sealyham terrier",
        "n02096051-Airedale" to "Airedale Terrier",
        "n02096177-cairn" to "Cairn Terrier",
        "n02096294-Australian_terrier" to "Terrier australiano",
        "n02096437-Dandie_Dinmont" to "Dandie Dinmont",
        "n02096585-Boston_bull" to "Boston terrier",
        "n02097047-miniature_schnauzer" to "Schnauzer",
        "n02097130-giant_schnauzer" to "Schnauzer",
        "n02097209-standard_schnauzer" to "Schnauzer",
        "n02097298-Scotch_terrier" to "Terrier escocés",
        "n02097474-Tibetan_terrier" to "Terrier tibetano",
        "n02097658-silky_terrier" to "Terrier australiano",
        "n02098105-soft-coated_wheaten_terrier" to "Terrier irlandés de pelo suave",
        "n02098286-West_Highland_white_terrier" to "Westie",
        "n02098413-Lhasa" to "Lhasa Apso",
        "n02099267-flat-coated_retriever" to "Cobrador de pelo liso",
        "n02099429-curly-coated_retriever" to "Cobrador de pelo liso",
        "n02099601-golden_retriever" to "Golden Retriever",
        "n02099712-Labrador_retriever" to "Labrador Retriever",
        "n02099849-Chesapeake_Bay_retriever" to "Retriever de Chesapeake",
        "n02100236-German_short-haired_pointer" to "Braco alemán",
        "n02100583-vizsla" to "Vizsla",
        "n02100735-English_setter" to "Setter inglés",
        "n02100877-Irish_setter" to "Setter irlandés",
        "n02101006-Gordon_setter" to "Gordon Setter",
        "n02101388-Brittany_spaniel" to "Spaniel bretón",
        "n02101556-clumber" to "Clumber Spaniel",
        "n02102040-English_springer" to "Springer Spaniel inglés",
        "n02102177-Welsh_springer_spaniel" to "Springer spaniel galés",
        "n02102318-cocker_spaniel" to "Cocker Spaniel inglés",
        "n02102480-Sussex_spaniel" to "Sussex Spaniel",
        "n02102973-Irish_water_spaniel" to "Perro de agua irlandés",
        "n02104029-kuvasz" to "Kuvasz",
        "n02104365-schipperke" to "Schipperke",
        "n02105056-groenendael" to "Pastor belga",
        "n02105162-malinois" to "Pastor belga",
        "n02105251-briard" to "Pastor de Brie",
        "n02105412-kelpie" to "Kelpie australiano",
        "n02105505-komondor" to "Komondor",
        "n02105641-Old_English_sheepdog" to "Bobtail",
        "n02105855-Shetland_sheepdog" to "Pastor de las islas Shetland",
        "n02106030-collie" to "Collie de pelo largo",
        "n02106166-Border_collie" to "Border collie",
        "n02106382-Bouvier_des_Flandres" to "Boyero de Flandes",
        "n02106550-Rottweiler" to "Rottweiler",
        "n02106662-German_shepherd" to "Pastor alemán",
        "n02107142-Doberman" to "Dóberman",
        "n02107312-miniature_pinscher" to "Pinscher",
        "n02107574-Greater_Swiss_Mountain_dog" to "Gran boyero suizo",
        "n02107683-Bernese_mountain_dog" to "Boyero de Berna",
        "n02107908-Appenzeller" to "Boyero de Appenzell",
        "n02108000-EntleBucher" to "Boyero de Entlebuch",
        "n02108089-boxer" to "Bóxer",
        "n02108422-bull_mastiff" to "Bullmastiff",
        "n02108551-Tibetan_mastiff" to "Dogo del Tíbet",
        "n02108915-French_bulldog" to "Bulldog francés",
        "n02109047-Great_Dane" to "Gran danés",
        "n02109525-Saint_Bernard" to "San Bernardo",
        "n02109961-Eskimo_dog" to "Perro de Groenlandia",
        "n02110063-malamute" to "Alaskan Malamute",
        "n02110185-Siberian_husky" to "Husky siberiano",
        "n02110627-affenpinscher" to "Affenpinscher",
        "n02110806-basenji" to "Basenji",
        "n02110958-pug" to "Pug",
        "n02111129-Leonberg" to "Leonberger",
        "n02111277-Newfoundland" to "Terranova",
        "n02111500-Great_Pyrenees" to "Perro de montaña de los Pirineos",
        "n02111889-Samoyed" to "Samoyedo",
        "n02112018-Pomeranian" to "Pomerania",
        "n02112137-chow" to "Chow Chow",
        "n02112350-keeshond" to "Pomerania",
        "n02112706-Brabancon_griffon" to "Grifón de Bruselas",
        "n02113023-Pembroke" to "Corgi galés de Pembroke",
        "n02113186-Cardigan" to "Corgi galés de Pembroke",
        "n02113624-toy_poodle" to "Caniche",
        "n02113712-miniature_poodle" to "Caniche",
        "n02113799-standard_poodle" to "Caniche",
        "n02113978-Mexican_hairless" to "Xoloitzcuintle",
    )

    override suspend fun map(labels: List<Pair<String, Float>>): List<BreedPrediction> {
        dataBaseSource.ensureSynced()
        val allDogs = loadAllDogs()
        return labels
            .sortedByDescending { it.second }
            .take(3)
            .map { (rawLabel, confidence) ->
                val dbName = labelToDbName[rawLabel]
                val match = dbName?.let { name ->
                    allDogs.firstOrNull { it.name == name }
                } ?: findMatch(rawLabel, allDogs)

                BreedPrediction(
                    label = rawLabel,
                    breedName = match?.name ?: rawLabel.substringAfter("-").replace("_", " "),
                    breedId = match?.let { getIdForDog(it) },
                    confidence = confidence,
                    imageUrl = match?.icon?.takeIf { it.isNotBlank() }
                )
            }
    }

    private fun loadAllDogs(): List<Dog> {
        return database.dogsQueries.getAll().executeAsList().map { row ->
            Dog(
                name = row.name,
                otherNames = row.otherNames,
                icon = row.icon
            )
        }
    }

    private fun getIdForDog(dog: Dog): Long? {
        val rows = database.dogsQueries.getAll().executeAsList()
        return rows.firstOrNull { it.name == dog.name }?.id
    }

    private fun findMatch(rawLabel: String, dogs: List<Dog>): Dog? {
        val readableName = rawLabel.substringAfter("-").replace("_", " ")
        val normalized = normalize(readableName)
        return dogs.firstOrNull { normalize(it.name) == normalized }
            ?: dogs.firstOrNull { normalized in normalize(it.name) || normalize(it.name) in normalized }
            ?: dogs.firstOrNull { matchOtherNames(normalized, it.otherNames) }
    }

    private fun matchOtherNames(normalized: String, otherNames: String): Boolean {
        if (otherNames.isBlank()) return false
        return otherNames.split(",", ";", "/")
            .map { normalize(it) }
            .any { it == normalized || normalized in it || it in normalized }
    }

    private fun normalize(name: String): String {
        val decomposed = Normalizer.normalize(name, Normalizer.Form.NFD)
        return decomposed
            .replace(Regex("\\p{InCombiningDiacriticalMarks}"), "")
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
    }
}
