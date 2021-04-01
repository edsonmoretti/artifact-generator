# Artifact Generator

Gerador automático de models com base na tabela do banco de dados.

Gerando automaticamente as classes e atributos.

Os atributos consideram o tipo de dados da coluna, por exemplo, doluna do tipo int receberá um tipo primitivo int, do tipo Decimal receberar um objeto java.math.BigDecimal, varchar do tipo String, Timestampt ou date receberá java.util.Date e assim por diante.
