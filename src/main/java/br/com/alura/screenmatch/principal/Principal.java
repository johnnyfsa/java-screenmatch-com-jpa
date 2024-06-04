package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }


    public void exibeMenu() {
        var opcao =-1;
        while(opcao!=0)
        {
        var menu = """
                1 - Buscar séries
                2 - Buscar episódios
                3 - Listas Series Buscadas
                4 - Procurar Serie no Banco de Dados por Título
                5 -  Buscar Séries por Ator
                6 - Top 5 Séries
                7 - Buscar séries por categoria
                8 - Buscar series por numero de temporadas
                9 - Buscar Episodio por Trecho
                10 - Top 5 Episodios por Serie
                11 - Buscar Episodios Por Data
                0 - Sair                                 
                """;

        System.out.println(menu);
            opcao = leitura.nextInt();

            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscaSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSerieTemporadaMaiorEAvaliacaoMaior();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    top5EpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosPorDataSuperior();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }

    }

    private void buscarEpisodiosPorDataSuperior() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent())
        {
            Serie serie = serieBuscada.get();
            System.out.println("Deseja procurar por episodios lançcados a partir de que ano?");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();
            List<Episodio> episodiosPorAno = repositorio.episodiosPorSerieEAno(serie,anoLancamento);
            episodiosPorAno.forEach(System.out::println);
        }
    }

    private void top5EpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent())
        {
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e->
                    System.out.printf("Serie: %s Temporada: %s - Episodio: %s - %s Avaliação: %f\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }

    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Insira o Nome ou trecho do nome do episódio para busca:");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e->
                System.out.printf("Serie: %s Temporada: %s - Episodio: %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarSerieTemporadaMaiorEAvaliacaoMaior() {
        System.out.println("Buscar Serie com numero de temporadas Maior que: ");
        int numTemp = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Buscar Series com avaliacao Maior que: ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> seriesEncontradas = repositorio.seriesPorTemporadaEAValiacao(numTemp, avaliacao);
        seriesEncontradas.forEach(System.out::println);
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Insira o nome do genero");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria:" + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarTop5Series() {
        List<Serie> topCinco = repositorio.findTop5ByOrderByAvaliacaoDesc();
        topCinco.forEach(s-> System.out.println(s.getTitulo() + "avaliação:" + s.getAvaliacao()));
    }

    private void buscaSeriesPorAtor() {
        System.out.println("Insira o nome de um dos atores:");
        var nomeAtor = leitura.nextLine();
        System.out.println("Filmes a partir de que nota? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        seriesEncontradas.forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome:");
        var nomeSerie = leitura.nextLine();
        List<Serie> seriesBuscadas = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        serieBuscada = seriesBuscadas.stream().findFirst();

        if (!seriesBuscadas.isEmpty())
        {
            System.out.println("Dados da serie: ");
            seriesBuscadas.forEach(System.out::println);
        }
        else
        {
            System.out.println("Serie não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma serie pelo nome:");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie =
                series.stream()
                .filter(s-> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent())
        {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d-> d.episodios().stream()
                            .map(e-> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }
        else
        {
            System.out.println("Série não encontrada");
        }

    }
}