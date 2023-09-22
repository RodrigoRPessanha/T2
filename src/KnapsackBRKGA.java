// Importando as bibliotecas necessárias
import java.util.*;
import java.io.*;

public class KnapsackBRKGA {

    // Para 4 itens usar POP_SIZE = 100 e NUM_GENERATIONS = 200
    // Para 30 itens usar POP_SIZE = 3000 e NUM_GENERATIONS = 5000
    // Para 40 itens usar POP_SIZE = 20000 e NUM_GENERATIONS = 15000 (não funciona ainda)

    // Parâmetros do algoritmo genético
    private static final int POP_SIZE = 100;              // Tamanho da população
    private static final int NUM_GENERATIONS = 200;       // Número de gerações
    private static final double MUTATION_RATE = 0.01;     // Taxa de mutação
    private static final double ELITE_RATE = 0.1;         // Percentual de indivíduos elite

    // Dados do problema
    private int numItems;             // Número de itens disponíveis
    private double[] weights;        // Peso de cada item
    private double[] values;         // Valor (ou benefício) de cada item
    private double capacity;         // Capacidade da mochila
    private List<Individual> population; // População de soluções

    // Construtor que lê os dados do problema de um arquivo
    public KnapsackBRKGA(String filename) throws FileNotFoundException {
        // Inicializando o leitor do arquivo
        Scanner scanner = new Scanner(new File(filename));
        
        // Lendo a capacidade da mochila e o número de itens
        this.capacity = Double.parseDouble(scanner.nextLine().trim());
        this.numItems = Integer.parseInt(scanner.nextLine().trim());
        this.weights = new double[numItems];
        this.values = new double[numItems];
        System.out.println("Capacidade: "+capacity);
        System.out.println("Quantidade de itens: "+ numItems);
        // Lendo o valor e o peso de cada item
        for (int i = 0; i < numItems; i++) {
            String[] line = scanner.nextLine().trim().split(" ");
            values[i] = Double.parseDouble(line[0]);
            weights[i] = Double.parseDouble(line[1]);
        }

        // Fechando o leitor do arquivo
        scanner.close();
        
        // Inicializando a população
        this.population = new ArrayList<>();
    }

    // Classe interna para representar um indivíduo (solução)
    public class Individual implements Comparable<Individual> {
        double[] genes;   // Chaves aleatórias que representam a solução
        double fitness;   // Valor da solução (0 se for inválido)

        // Construtor do indivíduo
        public Individual(double[] genes) {
            this.genes = genes;
            this.fitness = computeFitness(genes);
        }

        // Método para comparar indivíduos com base em seu fitness
        @Override
        public int compareTo(Individual other) {
            return Double.compare(other.fitness, this.fitness); // Ordem decrescente
        }
    }

    public long calculateTimeComplexity() {
        long totalOperations = 0;
        // Inicialização da população
        totalOperations += POP_SIZE * numItems;
        // Execução das gerações
        for (int generation = 0; generation < NUM_GENERATIONS; generation++) {
            // Ordenação da população
            totalOperations += POP_SIZE * log2(POP_SIZE); // log2 é uma função para calcular logaritmo na base 2
            // Elitismo
            totalOperations += ELITE_RATE * POP_SIZE * numItems;
            // Crossover e mutação
            totalOperations += (POP_SIZE - ELITE_RATE * POP_SIZE) * (numItems * 2); // Considera crossover e mutação
        }

        return totalOperations;
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    // Método para calcular o fitness (valor) de uma solução
    private double computeFitness(double[] genes) {
        double totalWeight = 0;
        double totalValue = 0;
        for (int i = 0; i < numItems; i++) {
            if (genes[i] > 0.5) {  // Decisão baseada no limiar
                totalWeight += weights[i];
                totalValue += values[i];
            }
        }
        // Se exceder a capacidade, retorna 0 (solução inválida)
        return totalWeight <= capacity ? totalValue : 0;
    }

    // Método para inicializar a população com soluções aleatórias
    private void initializePopulation() {
        for (int i = 0; i < POP_SIZE; i++) {
            double[] genes = new double[numItems];
            for (int j = 0; j < numItems; j++) {
                genes[j] = Math.random();
            }
            population.add(new Individual(genes));
        }
    }

    // Método para fazer o crossover (recombinação) entre dois pais
    private Individual crossover(Individual parent1, Individual parent2) {
        double[] childGenes = new double[numItems];
        for (int i = 0; i < numItems; i++) {
            // Fazendo a média das chaves dos pais
            childGenes[i] = (parent1.genes[i] + parent2.genes[i]) / 2.0;
        }
        return new Individual(childGenes);
    }

    // Método para mutar (alterar) algumas chaves de um indivíduo
    private void mutate(Individual individual) {
        for (int i = 0; i < numItems; i++) {
            if (Math.random() < MUTATION_RATE) {
                individual.genes[i] = Math.random();
            }
        }
    }

    // Método principal para executar o algoritmo genético
    public void run() {
        // Inicializando a população
        initializePopulation();

        // Executando o algoritmo para o número especificado de gerações
        for (int generation = 0; generation < NUM_GENERATIONS; generation++) {
            // Ordenando a população com base no fitness
            Collections.sort(population);

            // Preparando a próxima geração
            List<Individual> nextGen = new ArrayList<>();

            // Adicionando os melhores indivíduos diretamente (elitismo)
            int eliteCount = (int) (ELITE_RATE * POP_SIZE);
            for (int i = 0; i < eliteCount; i++) {
                nextGen.add(population.get(i));
            }

            // Produzindo novos indivíduos por crossover e mutação
            while (nextGen.size() < POP_SIZE) {
                int idx1 = (int) (Math.random() * eliteCount);
                int idx2 = (int) (Math.random() * (POP_SIZE - eliteCount)) + eliteCount;
                Individual child = crossover(population.get(idx1), population.get(idx2));
                mutate(child);
                nextGen.add(child);
            }

            // Atualizando a população
            population = nextGen;
        }

        // Imprimindo os itens selecionados do melhor indivíduo
        Individual best = population.get(0);
        System.out.println("\nItens selecionados para serem colocados na mochila:");
        int soma = 0;
        int j = 0;
        for (int i = 0; i < numItems; i++) {
            if (best.genes[i] > 0.5) {
                System.out.println("Item" + ": Valor = " + values[i] + ", Peso = " + weights[i]);
                soma += weights[i];
                j += values[i];
            }
        }
        System.out.println("\nValor Total: " + j + "\nPeso Total: " + soma);
    }

    // Método principal para execução
    public static void main(String[] args) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();

        // Executando o algoritmo
        try {
            // KnapsackBRKGA knapsackBRKGA = new KnapsackBRKGA("C:\\Users\\Rodrigo\\Desktop\\a/10k.txt");
            KnapsackBRKGA knapsackBRKGA = new KnapsackBRKGA("src/teste.txt");
            // Calcular a complexidade de tempo
            long timeComplexity = knapsackBRKGA.calculateTimeComplexity();
            
            // Imprimir a complexidade de tempo
            System.out.println("Número total de operações: " + timeComplexity);
            knapsackBRKGA.run();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println("Erro: "+e.getMessage());
        }

        long finishTime = System.currentTimeMillis();
        System.out.print("\nRun time: " + (finishTime - startTime) / 1000f);
    }
}