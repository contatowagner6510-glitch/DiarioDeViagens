 Diário de Viagens - Travel Diary App

O Diário de Viagens é um aplicativo móvel Android completo, desenvolvido em Kotlin, que permite aos usuários registrar e gerenciar suas viagens e os pontos visitados em cada destino. O projeto utiliza uma arquitetura moderna e integra serviços de nuvem para persistência de dados e armazenamento de mídia.

 Funcionalidades Principais

•
CRUD Completo: Gerenciamento de Viagens (criação, leitura, atualização e exclusão) e Pontos Visitados.

•
Mapeamento Interativo: Visualização dos pontos visitados em um mapa interativo utilizando a biblioteca OSMDroid.

•
Armazenamento em Nuvem: Utilização do Firebase Firestore para dados estruturados e Cloudinary para armazenamento seguro de fotos.

•
Geocodificação: Conversão de nomes de locais em coordenadas geográficas para exibição precisa no mapa.

•
Compartilhamento: Funcionalidade para gerar um texto formatado com os detalhes da viagem para compartilhamento em redes sociais ou aplicativos de mensagem.

•
Design Moderno: Interface de usuário construída com Material Design 3 e suporte a tema escuro (Dark Mode).

 Tecnologias Utilizadas

Categoria
Tecnologia
Descrição
Linguagem
Kotlin
Linguagem de programação oficial para desenvolvimento Android.
IDE
Android Studio
Ambiente de Desenvolvimento Integrado.
Banco de Dados
Firebase Firestore
NoSQL Cloud Database para persistência de dados em tempo real.
Armazenamento de Mídia
Cloudinary
Serviço de gerenciamento e otimização de imagens na nuvem.
Mapeamento
OSMDroid
Biblioteca para mapas baseados em OpenStreetMap (alternativa ao Google Maps).
Imagens
Coil
Biblioteca leve e rápida para carregamento de imagens.
UI/UX
Material Design 3
Componentes modernos e responsivos.
Geocodificação
Android Geocoder API
API nativa para conversão de endereços.


 Estrutura de Dados (Firebase Firestore)

O banco de dados é estruturado em coleções aninhadas:

•
viagens (Coleção Principal): Armazena os dados de cada viagem.

•
Documento de Viagem: Contém campos como nome, destino, dataInicio, dataFim, latitude, longitude.

•
pontosVisitados (Subcoleção): Aninhada em cada documento de viagem, armazena os pontos específicos visitados.

•
Documento de Ponto: Contém campos como nome, descricao, urlFotoCloudinary, latitude, longitude.





⚙️ Configuração e Instalação

Pré-requisitos

•
Android Studio (versão mais recente)

•
Conta Firebase (para Firestore)

•
Conta Cloudinary (para armazenamento de fotos)

Passos para Rodar o Projeto

1.
Clone o Repositório:

2.
Configuração do Firebase:

•
Crie um projeto no Firebase e configure o Firestore.

•
Baixe o arquivo google-services.json e coloque-o na pasta app/.



3.
Configuração do Cloudinary:

•
Obtenha suas credenciais (Cloud Name, API Key, API Secret).

•
Insira as credenciais no local apropriado no código (geralmente em um arquivo de constantes ou no build.gradle.kts).



4.
Sincronize e Execute:

•
Abra o projeto no Android Studio.

•
Sincronize o Gradle.

•
Execute o aplicativo em um emulador ou dispositivo físico.



