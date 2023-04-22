package get;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Test;
import pojo.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PokemonApiTest {

    /*
    1. GET https://pokeapi.co/api/v2/pokemon
    2. Validate you got 20 Pokemons
    3. Get every Pokemons ability and store those in Map<String, List<String>>
     */

    private static final String POKEMON_API_URI = "https://pokeapi.co/api/v2/pokemon";

    @Test
    public void testPokemonApi(){
        var pokemonResponse = getApiResponseAs(POKEMON_API_URI, PokemonResponse.class);

        Map<String, List<String>> abilityMap = new HashMap<>();
        pokemonResponse.getResults().stream()
                .map(this::getPokemonAbilities)
                .forEach(entry -> abilityMap.put(entry.getKey(),entry.getValue()));

        assertThat(pokemonResponse.getResults()).hasSize(20);
        assertAbilitiesAreRetrievedForEachPokemon(abilityMap);
    }

    private void assertAbilitiesAreRetrievedForEachPokemon(Map<String, List<String>> abilityMap) {
        assertThat(abilityMap.entrySet()).hasSize(20);
        abilityMap.keySet().stream().forEach(key -> assertThat(abilityMap.get(key)).isNotEmpty());
    }

    private <T> T getApiResponseAs(String apiUrl, Class<T> clazz) {
        RestAssured.baseURI = apiUrl;
        return RestAssured.given().accept(ContentType.JSON)
                .when().get()
                .then().statusCode(200)
                .extract().response()
                .as(clazz);
    }

    private Map.Entry<String, List<String>> getPokemonAbilities(Pokemon pokemon) {
        RestAssured.baseURI=pokemon.getUrl();
        var abilities =  getApiResponseAs(pokemon.getUrl(), Abilities.class);

        var abilityList = abilities.getAbilities().stream()
                .map(pa -> pa.getAbility().getName())
                .collect(Collectors.toList());

        return new AbstractMap.SimpleEntry<>(pokemon.getName(), abilityList);
    }
}
