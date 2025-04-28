# Rapport - Algorithme A*

## Auteurs
- Kilian Froidevaux
- Nicolas Bovard
- Groupe P

## Introduction

Ce rapport présente l'implémentation et l'analyse de l'algorithme A* appliqué à la recherche de chemins optimaux dans des labyrinthes générés algorithmiquement. L'objectif est de comparer les performances de plusieurs heuristiques et d'évaluer leur impact par rapport à l'algorithme de Dijkstra classique.

## Implémentation

### Algorithme A*

L'algorithme A* est une amélioration de l'algorithme de Dijkstra qui utilise une heuristique pour estimer la distance restante jusqu'à la destination. Notre implémentation propose cinq variantes d'heuristiques :

1. **H0 (DIJKSTRA)** : Équivalent à l'algorithme de Dijkstra (heuristique nulle)
2. **H1 (INFINITY_NORM)** : Utilise la norme L∞ (maximum des écarts en x et y)
3. **H2 (EUCLIDEAN_NORM)** : Utilise la distance euclidienne
4. **H3 (MANHATTAN)** : Utilise la distance de Manhattan (somme des écarts absolus)
5. **H4 (K_MANHATTAN)** : Utilise K fois la distance de Manhattan

Nous avons implémenté la fonction d'évaluation de l'heuristique dans la méthode `evaluate` :

```java
private double evaluate(int delta_x, int delta_y, double minWeight) {
    double result = 0;
    switch (this.heuristic) {
      case INFINITY_NORM:
        result = Math.max(delta_x, delta_y);
        break;
      case EUCLIDEAN_NORM:
        result = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
        break;
      case MANHATTAN:
        result = Math.abs(delta_x) + Math.abs(delta_y);
        break;
      case K_MANHATTAN:
        result = this.kManhattan * (Math.abs(delta_x) + Math.abs(delta_y));
        break;
      case DIJKSTRA:
        return 0;
    }
    return result * minWeight;
}
```

L'algorithme A* lui-même est implémenté dans la méthode `solve`, qui utilise une file de priorité pour traiter les sommets selon leur valeur f(n) = g(n) + h(n), où g(n) est le coût du chemin de la source au sommet n, et h(n) est l'estimation heuristique du coût de n à la destination.

### Génération des labyrinthes

Pour créer nos labyrinthes, nous avons réutilisé notre implémentation du générateur DFS du travail pratique précédent. Ce générateur utilise un parcours en profondeur pour créer des chemins dans le labyrinthe, garantissant qu'il existe au moins un chemin entre deux points quelconques.

Nous avons également amélioré le DFS suivant les indications du précédant labo.

## Résultats et analyse

### Comparaison des heuristiques H0-H3

Nous avons effectué des tests sur 100 labyrinthes différents pour chaque configuration de paramètres. Les résultats pour les quatre premières heuristiques (H0-H3) sont présentés ci-dessous.

#### Relief très peu dense, labyrinthe très ouvert
| Algorithme         | Longueur moyenne | Sommets traités | Réduction vs Dijkstra |
| ------------------ | ---------------- | --------------- | --------------------- |
| Dijkstra (H0)      | 242.70           | 64121.13        | Référence             |
| Infinity Norm (H1) | 242.72           | 17487.47        | 72.73%                |
| Euclidean (H2)     | 242.72           | 15014.23        | 76.58%                |
| Manhattan (H3)     | 242.70           | 9084.33         | 85.83%                |

#### Relief très peu dense, labyrinthe assez ouvert
| Algorithme         | Longueur moyenne | Sommets traités | Réduction vs Dijkstra |
| ------------------ | ---------------- | --------------- | --------------------- |
| Dijkstra (H0)      | 277.72           | 62430.30        | Référence             |
| Infinity Norm (H1) | 277.72           | 19777.57        | 68.32%                |
| Euclidean (H2)     | 277.72           | 17415.87        | 72.10%                |
| Manhattan (H3)     | 277.72           | 12095.84        | 80.63%                |

#### Relief très peu dense, labyrinthe peu ouvert
| Algorithme         | Longueur moyenne | Sommets traités | Réduction vs Dijkstra |
| ------------------ | ---------------- | --------------- | --------------------- |
| Dijkstra (H0)      | 1025.44          | 78575.00        | Référence             |
| Infinity Norm (H1) | 1025.44          | 54247.28        | 30.96%                |
| Euclidean (H2)     | 1025.44          | 52125.37        | 33.66%                |
| Manhattan (H3)     | 1025.44          | 47263.00        | 39.85%                |

#### Relief dense, labyrinthe moyennement ouvert
| Algorithme         | Longueur moyenne | Sommets traités | Réduction vs Dijkstra |
| ------------------ | ---------------- | --------------- | --------------------- |
| Dijkstra (H0)      | 488.06           | 82307.31        | Référence             |
| Infinity Norm (H1) | 488.30           | 58905.35        | 28.43%                |
| Euclidean (H2)     | 488.28           | 56797.62        | 30.99%                |
| Manhattan (H3)     | 488.22           | 51919.87        | 36.92%                |

#### Relief très dense, labyrinthe moyennement ouvert
| Algorithme         | Longueur moyenne | Sommets traités | Réduction vs Dijkstra |
| ------------------ | ---------------- | --------------- | --------------------- |
| Dijkstra (H0)      | 453.28           | 72589.76        | Référence             |
| Infinity Norm (H1) | 453.32           | 59441.56        | 18.11%                |
| Euclidean (H2)     | 453.30           | 58192.44        | 19.83%                |
| Manhattan (H3)     | 453.28           | 55068.03        | 24.14%                |

#### Relief très dense et fortement pondéré, labyrinthe moyennement ouvert
| Algorithme         | Longueur moyenne | Sommets traités | Réduction vs Dijkstra |
| ------------------ | ---------------- | --------------- | --------------------- |
| Dijkstra (H0)      | 534.18           | 96030.40        | Référence             |
| Infinity Norm (H1) | 534.18           | 88523.68        | 7.82%                 |
| Euclidean (H2)     | 534.18           | 87703.20        | 8.67%                 |
| Manhattan (H3)     | 534.18           | 85741.42        | 10.71%                |

### Observations (H0-H3)

1. **Optimalité des chemins** : Pour les heuristiques H0-H3, les longueurs de chemin sont quasi identiques (à quelques variations minimes près), ce qui confirme que ces heuristiques sont admissibles et produisent des chemins optimaux.

2. **Efficacité des heuristiques** : Dans tous les cas, les heuristiques A* réduisent considérablement le nombre de sommets traités par rapport à Dijkstra :
   - L'heuristique Manhattan (H3) est la plus efficace, avec une réduction allant de 10.71% à 85.83% selon la configuration.
   - L'heuristique Euclidienne (H2) offre une performance intermédiaire.
   - L'heuristique de la norme infinie (H1) est généralement la moins efficace des trois.

3. **Influence de la configuration du labyrinthe** : Les gains de performance sont plus importants dans les labyrinthes très ouverts avec un relief peu dense (jusqu'à 85.83% de réduction). À l'inverse, dans les configurations avec un relief très dense et fortement pondéré, les gains sont plus modestes (environ 10%).

4. **Effet du relief** : La densité du relief et la pondération des arêtes semblent avoir un impact significatif sur l'efficacité des heuristiques. Plus le relief est dense et pondéré, moins les heuristiques sont efficaces par rapport à Dijkstra.

### Expériences avec K-Manhattan (H4)

Pour compléter notre analyse, nous avons également exécuté des tests avec l'heuristique K-Manhattan (H4) en faisant varier K de 2 à 8. Cette heuristique n'étant pas admissible pour K > 1, elle ne garantit pas l'optimalité des chemins trouvés, mais peut potentiellement réduire davantage le nombre de sommets traités.

Voici les résultats complets obtenus pour toutes les configurations testées :

#### Relief très peu dense, labyrinthe très ouvert

| K   | % Optimal | Min Error | Avg Error | Max Error | Sommets traités | % Réduction |
| --- | --------- | --------- | --------- | --------- | --------------- | ----------- |
| 2   | 0.00%     | 0.0157    | 0.1343    | 0.3013    | 552.59          | 99.15%      |
| 3   | 0.00%     | 0.0389    | 0.1741    | 0.4156    | 481.23          | 99.26%      |
| 4   | 0.00%     | 0.0586    | 0.2341    | 0.5103    | 457.86          | 99.30%      |
| 5   | 0.00%     | 0.0395    | 0.2358    | 0.4903    | 447.13          | 99.31%      |
| 6   | 0.00%     | 0.0395    | 0.2699    | 0.5749    | 440.81          | 99.32%      |
| 7   | 0.00%     | 0.0608    | 0.2915    | 0.7287    | 457.47          | 99.30%      |
| 8   | 0.00%     | 0.0711    | 0.3051    | 0.6180    | 452.16          | 99.30%      |

#### Relief très peu dense, labyrinthe assez ouvert

| K   | % Optimal | Min Error | Avg Error | Max Error | Sommets traités | % Réduction |
| --- | --------- | --------- | --------- | --------- | --------------- | ----------- |
| 2   | 0.00%     | 0.0077    | 0.1009    | 0.2890    | 919.92          | 98.51%      |
| 3   | 0.00%     | 0.0268    | 0.1936    | 0.5174    | 671.51          | 98.91%      |
| 4   | 0.00%     | 0.0212    | 0.2344    | 0.6304    | 635.25          | 98.97%      |
| 5   | 0.00%     | 0.0613    | 0.2649    | 0.5121    | 594.43          | 99.04%      |
| 6   | 0.00%     | 0.0233    | 0.2670    | 0.5809    | 578.66          | 99.06%      |
| 7   | 0.00%     | 0.0767    | 0.3058    | 0.7159    | 589.25          | 99.05%      |
| 8   | 0.00%     | 0.0837    | 0.3147    | 0.6207    | 594.20          | 99.04%      |

#### Relief très peu dense, labyrinthe peu ouvert

| K   | % Optimal | Min Error | Avg Error | Max Error | Sommets traités | % Réduction |
| --- | --------- | --------- | --------- | --------- | --------------- | ----------- |
| 2   | 64.00%    | 0.0000    | 0.0017    | 0.0203    | 27357.26        | 58.78%      |
| 3   | 38.00%    | 0.0000    | 0.0107    | 0.0739    | 18021.57        | 72.84%      |
| 4   | 14.00%    | 0.0000    | 0.0333    | 0.1591    | 11558.95        | 82.58%      |
| 5   | 5.00%     | 0.0000    | 0.0541    | 0.4780    | 8088.71         | 87.81%      |
| 6   | 2.00%     | -0.0795   | 0.0661    | 0.3014    | 8188.43         | 87.66%      |
| 7   | 7.00%     | 0.0000    | 0.0959    | 0.5211    | 6057.58         | 90.87%      |
| 8   | 4.00%     | 0.0000    | 0.1271    | 0.5691    | 5651.97         | 91.48%      |

#### Relief dense, labyrinthe moyennement ouvert

| K   | % Optimal | Min Error | Avg Error | Max Error | Sommets traités | % Réduction |
| --- | --------- | --------- | --------- | --------- | --------------- | ----------- |
| 2   | 45.00%    | -0.1386   | 0.0032    | 0.0764    | 34144.08        | 46.17%      |
| 3   | 24.00%    | -0.4428   | 0.0128    | 0.4059    | 21772.22        | 65.67%      |
| 4   | 8.00%     | -0.4466   | 0.0076    | 0.2249    | 16083.67        | 74.64%      |
| 5   | 10.00%    | -0.4950   | 0.0128    | 0.3692    | 11828.12        | 81.35%      |
| 6   | 6.00%     | -0.5034   | -0.0338   | 0.3255    | 8125.04         | 87.19%      |
| 7   | 2.00%     | -0.4835   | -0.0134   | 0.3940    | 7084.21         | 88.83%      |
| 8   | 2.00%     | -0.5053   | -0.0004   | 0.5663    | 4531.74         | 92.86%      |

#### Relief très dense, labyrinthe moyennement ouvert

| K   | % Optimal | Min Error | Avg Error | Max Error | Sommets traités | % Réduction |
| --- | --------- | --------- | --------- | --------- | --------------- | ----------- |
| 2   | 82.00%    | -0.0235   | 0.0008    | 0.0234    | 38694.60        | 36.81%      |
| 3   | 59.00%    | -0.0609   | 0.0029    | 0.0661    | 30775.25        | 49.74%      |
| 4   | 50.00%    | -0.4632   | 0.0004    | 0.0907    | 23480.88        | 61.65%      |
| 5   | 42.00%    | -0.3093   | 0.0072    | 0.2105    | 18223.14        | 70.24%      |
| 6   | 22.00%    | -0.4750   | -0.0235   | 0.1365    | 14525.75        | 76.28%      |
| 7   | 26.00%    | -0.2447   | -0.0043   | 0.2292    | 11061.46        | 81.94%      |
| 8   | 12.00%    | -0.3986   | -0.0186   | 0.2062    | 7864.49         | 87.16%      |

### Observations (K-Manhattan)

1. **Compromis optimalité-performance** : Dans la plupart des configurations, l'augmentation de K entraîne une diminution significative du nombre de sommets traités, mais souvent au détriment de l'optimalité des solutions.

2. **Variabilité selon la configuration** : 
   - Dans les labyrinthes très ouverts (première et deuxième configurations), aucune solution optimale n'est trouvée, même avec K=2. Toutefois, la réduction du nombre de sommets traités est spectaculaire (>98%).
   - Dans les labyrinthes peu ouverts et ceux avec un relief dense, l'heuristique K-Manhattan avec K=2 trouve encore un pourcentage significatif de solutions optimales (64% dans le labyrinthe peu ouvert, 45% et 82% dans ceux avec relief dense).

3. **Présence d'erreurs négatives** : L'erreur négative vient surement du fait que l'on mesure le nombre de sommets et non pas la distance.

4. **Compromis exceptionnels** : 
   - Dans la configuration "Relief très dense, labyrinthe moyennement ouvert", K=2 offre un excellent compromis avec 82% de solutions optimales et une réduction de 36.81% des sommets traités.
   - Dans le "Relief très peu dense, labyrinthe peu ouvert", K=2 maintient 64% de solutions optimales avec une réduction de 58.78%.

5. **Rendement marginal décroissant** : Au-delà de K=5, les gains supplémentaires en termes de réduction de sommets traités deviennent moins significatifs, alors que la perte d'optimalité continue d'augmenter.

## Conclusion

Notre étude démontre que l'utilisation d'heuristiques dans l'algorithme A* permet de réduire considérablement le nombre de sommets traités par rapport à l'algorithme de Dijkstra. Les heuristiques admissibles (H1-H3) garantissent l'optimalité des chemins trouvés tout en offrant des gains de performance significatifs, particulièrement l'heuristique de Manhattan (H3) qui s'est révélée la plus efficace.

L'heuristique K-Manhattan non admissible (avec K > 1) offre des compromis intéressants qui varient selon la configuration du labyrinthe :
- Dans certains contextes, elle peut réduire le nombre de sommets traités de plus de 99%, mais au prix d'une perte complète d'optimalité.
- Dans d'autres configurations, notamment celles avec relief dense, elle peut maintenir un bon taux de solutions optimales tout en réduisant significativement le nombre de sommets traités.

Ces résultats montrent que le choix de l'heuristique et de ses paramètres doit être adapté au contexte spécifique de l'application :
- Pour des applications nécessitant une optimalité absolue, les heuristiques admissibles comme Manhattan (H3) sont préférables.
- Pour des applications où la vitesse de calcul prime sur l'optimalité absolue (jeux vidéo, navigation en temps réel), l'heuristique K-Manhattan avec K=2 ou K=3 peut offrir un excellent compromis selon la configuration du terrain.

De plus, nos expériences ont mis en évidence l'importance de la structure du terrain et de sa pondération sur l'efficacité des heuristiques. Les gains les plus spectaculaires sont observés dans les environnements ouverts avec peu de relief, tandis que les terrains fortement pondérés avec relief dense limitent l'efficacité des heuristiques.

En conclusion, l'algorithme A* avec une heuristique bien choisie constitue une amélioration majeure par rapport à Dijkstra pour la recherche de chemins dans des graphes pondérés, mais le choix optimal de l'heuristique dépend fortement des caractéristiques du problème et des priorités entre optimalité et performance.

